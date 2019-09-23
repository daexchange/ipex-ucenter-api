package ai.turbochain.ipex.controller;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.ExangeService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberExclusiveFeeService;
import ai.turbochain.ipex.service.MemberLevelFeeService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.util.Md5;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.util.RSAUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/mobile-exange/asset")
@Slf4j
public class MobileExangeAssetController {
	
	public static final Integer RESULT_FAIL_CODE = 1;
	
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ExangeService exangeService;
    @Autowired
    private LocaleMessageSourceService messageSourceService; 
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberLevelFeeService memberLevelFeeService;
    @Autowired
    private MemberExclusiveFeeService memberExclusiveFeeService;
    @Value("${rsa.certs.path}")
    private String certsPath;
	@Value("${rsa.keystore.path}")
    private String keystorePath;
	
    boolean checkJyPassword(Member memberFrom, String jyPassword) throws Exception {
       
    	hasText(jyPassword, messageSourceService.getMessage("MISSING_JYPASSWORD"));
    	
    	String mbPassword = memberFrom.getJyPassword();
         
        Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + memberFrom.getSalt()).toLowerCase().equals(mbPassword), messageSourceService.getMessage("ERROR_JYPASSWORD"));
       
        return true;
    }
    
    
    /**
     * 快速支付
     * @param user
     * @return //sign = RSAUtil.generateSignature(map,certsPath);
     * @throws Exception
     */
    @RequestMapping("/quick-pay")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult quickPay( 
    		@RequestParam(value = "coinId", required = true) String coinId, 
    		@RequestParam(value = "payerEmail", required = true) String payerEmail,
    		@RequestParam(value = "payeeEmail", required = true) String payeeEmail,
    		@RequestParam(value = "amount", required = true)  BigDecimal amount,
    		@RequestParam(value = "sign", required = false) byte[] sign) throws Exception {
            
    	MessageResult messageResult = null;
    	
    	try {
    		if (sign!=null) {
    			Map<String, Object> map = new HashMap<String, Object>();
        		
        		map.put("coinId",coinId);
        		map.put("payerEmail",payerEmail);
        		map.put("payeeEmail",payeeEmail);
        		map.put("amount",amount);
        		
        		String str = RSAUtil.keySort(map);
        		
        		// 验签
        		if(!str.equals(RSAUtil.decrypt(sign,keystorePath))) {
            		return new MessageResult(RESULT_FAIL_CODE, "签名验证失败！");
        		}
    		}
    		
    		if (payerEmail.equals(payeeEmail)) {
                return new MessageResult(RESULT_FAIL_CODE, "不能转账给本人");
            }
    		
        	// 判断改货币是否支持转账
        	Coin coin = (Coin) coinService.findOne(coinId);
        	
        	notNull(coin, messageSourceService.getMessage("COIN_ILLEGAL"));
        	
        	if (coin.getStatus().equals(CommonStatus.ILLEGAL) && BooleanEnum.IS_FALSE.equals(coin.getCanTransfer())) {
        		return new MessageResult(RESULT_FAIL_CODE, "当前币种不支持转账");
        	}
        	
    		Member memberFrom = memberService.findByEmail(payerEmail);
    		
    		if (memberFrom==null) {
        		// TODO 国际化
        		return MessageResult.error("该邮件地址尚未注册会员！");
        	}
    		
        	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberFrom.getId());
            
            isTrue(memberWallet.getIsLock()==BooleanEnum.IS_FALSE,"钱包已锁定");

        	if (memberWallet.getBalance().compareTo(amount) < 0) {
                return new MessageResult(RESULT_FAIL_CODE, "余额不足");
            }
        	
        	Member memberTo = memberService.findByEmail(payeeEmail);
        	
        	if (memberTo==null) {
        		// TODO 国际化
        		return MessageResult.error("该邮件地址尚未注册会员！");
        	}

        	BigDecimal fee =  BigDecimal.ZERO;
        	
        	if(fee == null){
        		return new MessageResult(RESULT_FAIL_CODE, "转账失败！");
        	}
        	
        	// 资金划转
        	messageResult = exangeService.transferToOther(coinId, amount,fee, memberFrom.getId(), memberTo.getId());
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		messageResult = new MessageResult(500,e.getMessage());
    	}
    	
    	return messageResult;
    }
   
   /**
    * 查询币币账户
    * @param user
    * @return
    */
   @RequestMapping("/wallet")
   public MessageResult queryWallet(
		@RequestParam(value = "email", required = true) String email,
   		@RequestParam(value = "coinId", required = false) String coinId ) {
		  
	   MessageResult result = MessageResult.success();
	  
	   Member memberFrom = memberService.findByEmail(email);
		
	   if (memberFrom==null) {
   		// TODO 国际化
   		return MessageResult.error("该邮件地址尚未注册会员！");
	   }
	   
	   Long memberId = memberFrom.getId();
	 
	   if (StringUtils.isBlank(coinId)) {
		   List<MemberWallet>  list = memberWalletService.findAllByMemberId(memberId);
		   result.setData(list);
	   } else {
		   Coin coin = new Coin();
		   coin.setName(coinId);
		   MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, memberId);
		   result.setData(memberWallet);
	   }
       return result;
   }
   
}
