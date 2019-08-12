package ai.turbochain.ipex.controller;

import static ai.turbochain.ipex.util.MessageResult.error;
import static ai.turbochain.ipex.util.MessageResult.success;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.constant.MemberLevelEnum;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Country;
import ai.turbochain.ipex.entity.Location;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.MobileRegisterByEmail;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.util.BindingResultUtil;
import ai.turbochain.ipex.util.IdWorkByTwitter;
import ai.turbochain.ipex.util.Md5;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/mobile-register")
public class MobileRegisterController {
	private String userNameFormat = "U%06d";
	private Integer Origin_Mobile = 1;
	private Logger logger = LoggerFactory.getLogger(MobileRegisterController.class);
	@Autowired
    private MemberService memberService;
	@Resource
    private LocaleMessageSourceService localeMessageSourceService;
	@Autowired
    private IdWorkByTwitter idWorkByTwitter;
	@Autowired
	private ExecutorService executorService;
	@Autowired
	private CoinService coinService;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MemberWalletService memberWalletService;
	@Autowired
	private MemberLegalCurrencyWalletService memberLegalCurrencyWalletService;
	
	
	 /**
     * 邮箱注册
     *
     * @param loginByEmail
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @RequestMapping("/email")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult registerByEmail(
            @Valid MobileRegisterByEmail mobileRegisterByEmail,
            BindingResult bindingResult,HttpServletRequest request) throws Exception {
        
    	MessageResult result = BindingResultUtil.validate(bindingResult);
       
        if (result != null) {
            return result;
        }
        
        String ip = request.getHeader("X-Real-IP");
        String email = mobileRegisterByEmail.getEmail();
        
        // TODO 设置IP白名单
        // TODO 每一分钟调用不得超过60次
        
        Member member = memberService.findByEmail(email);
        
        if (member!=null) {
        	// 更新昵称
        //	if (!mobileRegisterByEmail.getUsername().equals(member.getUsername())) {
        //		member.setUsername(mobileRegisterByEmail.getUsername());
        //	}
           
        	//不可重复随机数
            String loginNo = String.valueOf(idWorkByTwitter.nextId());
            //盐
            String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
            //生成密码
            String password = Md5.md5Digest(mobileRegisterByEmail.getPassword() + credentialsSalt).toLowerCase();
            
            member.setPassword(password);// 更新密码
        	
            return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
        } else {
        	// 注册时不填写用户名，因此不需要判断用户名是否已经存在
            //isTrue(!memberService.usernameIsExist(loginByEmail.getUsername()), localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));

            Member member1 = memberService.save(getMember(mobileRegisterByEmail));
            
            if (member1 != null) {
            	afterRegister(member1.getId());
                return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
            } else {
                return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
            }
        }
    }
    
    Member getMember(MobileRegisterByEmail mobileRegisterByEmail) throws Exception {
    	Member member = new Member();
    	//不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
        //生成密码
        String password = Md5.md5Digest(mobileRegisterByEmail.getPassword() + credentialsSalt).toLowerCase();
    	Location location = new Location();
        Country country = new Country();
        location.setCountry(null);
        country.setZhName(null);
        member.setCountry(null);
        member.setLocation(null);
        
        member.setStatus(CommonStatus.NORMAL);
        member.setMemberLevel(MemberLevelEnum.GENERAL);
      //  member.setUsername(mobileRegisterByEmail.getUsername());
        member.setPassword(password);
        member.setEmail(mobileRegisterByEmail.getEmail());
        member.setSalt(credentialsSalt);
        member.setOrigin(Origin_Mobile);//代表手机端
        
        return member;
    }
    
    
	/**
	 * 注册成功后的操作
	 */
	public void afterRegister(Long memberId){
        executorService.execute(new Runnable() {
            public void run() {
            	registerCoin(memberId);
            }
        });
    }
	
	public void registerCoin(Long memberId) {
		// 获取所有支持的币种
		List<Coin> coins = coinService.findAll();
		for (Coin coin : coins) {
			MemberWallet wallet = new MemberWallet();
			wallet.setCoin(coin);
			wallet.setMemberId(memberId);
			wallet.setBalance(new BigDecimal(0));
			wallet.setFrozenBalance(new BigDecimal(0));
			wallet.setAddress("");
            if(coin.getEnableRpc() == BooleanEnum.IS_TRUE) {
                String account = "U" + memberId;
                //远程RPC服务URL,后缀为币种单位
                String serviceName = "SERVICE-RPC-" + coin.getUnit();
                try{
                    String url = "http://" + serviceName + "/rpc/address/{account}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                    logger.info("remote call:service={},result={}", serviceName, result);
                    if (result.getStatusCode().value() == 200) {
                        MessageResult mr = result.getBody();
                        logger.info("mr={}", mr);
                        if (mr.getCode() == 0) {
                            //返回地址成功，调用持久化
                            String address = (String) mr.getData();
                            wallet.setAddress(address);
                        }
                    }
                }
                catch (Exception e){
                    logger.error("call {} failed,error={}",serviceName,e.getMessage());
                    wallet.setAddress("");
                }
            } else {
                wallet.setAddress("");
            }
            
			// 保存
            memberWalletService.save(wallet);

			MemberLegalCurrencyWallet memberLegalCurrencyWallet = new MemberLegalCurrencyWallet();

			memberLegalCurrencyWallet.setCoin(coin);
			memberLegalCurrencyWallet.setMemberId(memberId);
			memberLegalCurrencyWallet.setBalance(BigDecimal.ZERO);
			memberLegalCurrencyWallet.setFrozenBalance(BigDecimal.ZERO);
			memberLegalCurrencyWallet.setToReleased(BigDecimal.ZERO);
			
			memberLegalCurrencyWalletService.save(memberLegalCurrencyWallet);
		}
	}
	
}