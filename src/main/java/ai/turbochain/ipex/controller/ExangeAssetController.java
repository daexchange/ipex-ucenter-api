package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.CertifiedBusinessStatus;
import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.constant.RealNameStatus;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.ExangeService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberAddressService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.util.Md5;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/exange/asset")
@Slf4j
public class ExangeAssetController {
	
	public static final Integer RESULT_FAIL_CODE = 1;
	
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ExangeService exangeService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LocaleMessageSourceService messageSourceService; 
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberAddressService memberAddressService;
    
    boolean checkJyPassword(Member memberFrom, String jyPassword) throws Exception {
       
    	hasText(jyPassword, messageSourceService.getMessage("MISSING_JYPASSWORD"));
    	
    	String mbPassword = memberFrom.getJyPassword();
         
        Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + memberFrom.getSalt()).toLowerCase().equals(mbPassword), messageSourceService.getMessage("ERROR_JYPASSWORD"));
       
        return true;
    }
    
    
    /**
     * 转账  暂时去掉手机验证码验证
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transfer(@SessionAttribute(SESSION_MEMBER) AuthMember user, 
    		String coinId, String email,
            BigDecimal amount, BigDecimal fee, @RequestParam("code") String code, String jyPassword) throws Exception {
    	MessageResult messageResult = null;
    	
    	/*String key = SysConstant.EMAIL_EXANGE_TRANSFER_PREFIX  + user.getEmail();
    	
    	Object cache = redisTemplate.opsForValue().get(key);
        
    	if (null == cache || !code.equals(cache.toString())) {
            return MessageResult.error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        */
    	
    	try {
    		Long memberId = user.getId();
        	
        	if (user.getEmail().equals(email)) {
                return new MessageResult(RESULT_FAIL_CODE, "不能转账给本人");
            }
        	// 判断改货币是否支持转账
        	Coin coin = (Coin) coinService.findByUnit(coinId);
        	
        	notNull(coin, messageSourceService.getMessage("COIN_ILLEGAL"));
        	
        	if (coin.getStatus().equals(CommonStatus.ILLEGAL) && BooleanEnum.IS_FALSE.equals(coin.getCanTransfer())) {
        		return new MessageResult(RESULT_FAIL_CODE, "当前币种不支持转账");
        	}
        	
    		Member memberFrom = memberService.findOne(memberId);
    		
    		//校验交易密码
            checkJyPassword(memberFrom, jyPassword);
           
        	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
            
            isTrue(memberWallet.getIsLock()==BooleanEnum.IS_FALSE,"钱包已锁定");

        	if (memberWallet.getBalance().compareTo(amount) < 0) {
                return new MessageResult(RESULT_FAIL_CODE, "余额不足");
            }
        	
        	Member memberTo = memberService.findByEmail(email);
        	
        	if (memberTo==null) {
        		// TODO 国际化
        		return MessageResult.error("该邮件尚未注册会员！");
        	}

        	// 1.限制转账
            checkMemberTransferLimit(memberFrom,messageSourceService);
        	checkMemberTransferToLimit(memberTo,messageSourceService);
        	
        	// 2.扣减手续费
        	if(fee == null|| fee.compareTo(coin.getMinerFee())!=0){
        		return new MessageResult(RESULT_FAIL_CODE, "转账失败！");
        	}
        	
        	messageResult = exangeService.transferToOther(memberWallet,coinId, memberId, memberTo.getId(), amount,fee);
    	} catch (Exception e) {
    		e.printStackTrace();
    		messageResult = new MessageResult(500,e.getMessage());
    	}
    	
    	return messageResult;
    }
    
    
   public static void checkMemberTransferLimit(final Member member,LocaleMessageSourceService messageSourceService) {
	   
	   /**
        * 0表示禁止交易
        */
       Assert.isTrue(BooleanEnum.IS_TRUE.equals(member.getTransactionStatus()), "该账户已被禁止交易");

       String mbPassword = member.getJyPassword();
       
       Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
   
       /**
        * 认证商家状态
        */
       Assert.isTrue(CertifiedBusinessStatus.VERIFIED.equals(member.getCertifiedBusinessStatus()),"请先认证商家");
        
       /**
        * 实名认证
        */
       Assert.isTrue(RealNameStatus.VERIFIED.equals(member.getRealNameStatus()),"请先实名认证");

       /**
        * 投诉过多
        */
       if (member.getAppealTimes()>1) {
           Assert.isTrue(member.getAppealSuccessTimes()>(member.getAppealTimes()/2),"投诉过多，禁止交易");
       }
    
       /**
        * 账户状态
        */
       Assert.isTrue(CommonStatus.NORMAL.equals(member.getStatus()),"账号目前状态不合法");

   }
    
   
   public static void checkMemberTransferToLimit(final Member memberTo,LocaleMessageSourceService messageSourceService) {
	   
	   /**
        * 0表示禁止交易
        */
       Assert.isTrue(BooleanEnum.IS_TRUE.equals(memberTo.getTransactionStatus()), "转账账户已被禁止交易");

       String mbPassword = memberTo.getJyPassword();
       
       Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
   
       /**
        * 认证商家状态
        */
      // Assert.isTrue(CertifiedBusinessStatus.VERIFIED.equals(memberTo.getCertifiedBusinessStatus()),"转账账户尚未认证商家");
        
       /**
        * 实名认证
        */
       Assert.isTrue(RealNameStatus.VERIFIED.equals(memberTo.getRealNameStatus()),"转账账户尚未实名认证");

       /**
        * 投诉过多
        */
       if (memberTo.getAppealTimes()>1) {
           Assert.isTrue(memberTo.getAppealSuccessTimes()>(memberTo.getAppealTimes()/2),"转账账户被投诉过多，禁止交易");
       }
    
       /**
        * 账户状态
        */
       Assert.isTrue(CommonStatus.NORMAL.equals(memberTo.getStatus()),"转账账户状态不合法");

   }
   
   
   /**
    * 查询币币账户
    * @param user
    * @return
    */
   @RequestMapping("/wallet")
   public MessageResult queryWallet(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
	   MessageResult result = MessageResult.success();
	   
	   List<MemberWallet> list = memberWalletService.findAllByMemberId(user.getId());
      
	   result.setData(list);
       
       return result;
   }
}
