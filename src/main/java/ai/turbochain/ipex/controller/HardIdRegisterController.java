package ai.turbochain.ipex.controller;

import static ai.turbochain.ipex.util.MessageResult.error;
import static ai.turbochain.ipex.util.MessageResult.success;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.constant.MemberLevelEnum;
import ai.turbochain.ipex.constant.MemberRegisterOriginEnum;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Country;
import ai.turbochain.ipex.entity.HardIdRegister;
import ai.turbochain.ipex.entity.Location;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.MemberWallet;
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

@RestController
@Slf4j
@RequestMapping("/hard-id")
public class HardIdRegisterController {
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
     * 注册
     *
     * @param HardIdRegister
     * @param bindingResult
     * @throws Exception
     */
	//@PostMapping(value ="/saveNone")
	@RequestMapping(value ="/saveNone")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult save(HttpServletRequest request) throws Exception {
        
		// TODO 
		log.info("register start");
		
		String ip = request.getHeader("X-Real-IP");
        
        log.info("request ip:"+ip);
		
		HardIdRegister hardIdRegister = new HardIdRegister();
		
		//HardId不需要密码
		hardIdRegister.setPassword("123456");
				
        Member member = memberService.save(getMember(hardIdRegister));
        
        Long memberId = member.getId();
       
        log.info("register end");
       
        if (member != null) {
        	Map<String,Long> map = new HashMap<String,Long>();
        	
            map.put("memberId", memberId);
        	
            afterRegister(memberId);
        	
            MessageResult mr = new MessageResult();
            
            mr.setCode(0);
            mr.setMessage(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
            mr.setData(map);
            
            return mr;
        } else {
            return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        } 
    }
	
	
	/**
     * 绑定手机或邮箱
     *
     * @param HardIdRegister
     * @param bindingResult
     * @throws Exception
     */
	//@PostMapping(value ="/{memberId}/bind")
	@RequestMapping(value ="/{memberId}/bind")
    public MessageResult register(@PathVariable Long memberId,
    		@Valid HardIdRegister hardIdRegister,
            BindingResult bindingResult,HttpServletRequest request) throws Exception {
        
		log.info("bind start");
		
		//HardId不需要密码
		hardIdRegister.setPassword("123456");
		
        MessageResult result = BindingResultUtil.validate(bindingResult);
       
        if (result != null) {
            return result;
        }
        
        String ip = request.getHeader("X-Real-IP");
        
        log.info("request ip:"+ip);
        
        String email = hardIdRegister.getEmail();
        String mobilePhone = hardIdRegister.getMobilePhone();
        
        if (StringUtils.isBlank(mobilePhone)&&StringUtils.isBlank(email)) {
        	return error("请输入绑定手机号和邮箱地址");
        } else {
        	Member memberOld = (Member) memberService.findOne(memberId);
        	
        	if (memberOld==null) { // 新增
        		return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        	} else {
        		if (MemberRegisterOriginEnum.HARDID.getSourceType().intValue()!=memberOld.getOrigin().intValue()) {
                	return error("当前邮箱或手机号已注册");
                }
        		
        		memberOld.setEmail(hardIdRegister.getEmail());
        		memberOld.setMobilePhone(hardIdRegister.getMobilePhone());
        	    
        		try {
        			memberService.save(memberOld);
        		}catch(Exception e) {
        			e.printStackTrace();
        			return error("请手机号或邮箱地址已被绑定");
        		}
                return success("绑定成功");
        	}
        }
    }
	
	
	/**
     * 注册
     *
     * @param HardIdRegister
     * @param bindingResult
     * @throws Exception
     */
	@PostMapping(value ="/register")
	//@RequestMapping(value ="/register")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult register(@Valid HardIdRegister hardIdRegister,
            BindingResult bindingResult,HttpServletRequest request) throws Exception {
        
		log.info("register start");
		
		//HardId不需要密码
		hardIdRegister.setPassword("123456");
		
        MessageResult result = BindingResultUtil.validate(bindingResult);
       
        if (result != null) {
            return result;
        }
        
        String ip = request.getHeader("X-Real-IP");
        
        log.info("request ip:"+ip);
        
        String email = hardIdRegister.getEmail();
        String mobilePhone = hardIdRegister.getMobilePhone();
        
        if (StringUtils.isBlank(mobilePhone)&&StringUtils.isBlank(email)) {
        	return error("请输入绑定手机号和邮箱地址");
        } else {
        	Member member = null;
        	if (StringUtils.isNotBlank(mobilePhone)&&StringUtils.isNotBlank(email)) {
        		member = memberService.findMemberByMobilePhoneOrEmail(mobilePhone, email);
        	} else if (StringUtils.isNotBlank(mobilePhone)) {
        		member = memberService.findByPhone(mobilePhone);
        	} else if (StringUtils.isNotBlank(email)) {
        		member = memberService.findByEmail(email);
        	} else {
        		return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        	}
        	
        	if (member==null) { // 新增

                Member member1 = memberService.save(getMember(hardIdRegister));
                
                log.info("register end");
               
                if (member1 != null) {
                	afterRegister(member1.getId());
                    return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
                } else {
                    return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
                }
        	} else {
        		if (MemberRegisterOriginEnum.HARDID.getSourceType().intValue()!=member.getOrigin().intValue()) {
                	return error("当前邮箱或手机号已注册");
                }
        		
        		member.setEmail(hardIdRegister.getEmail());
        	    member.setMobilePhone(hardIdRegister.getMobilePhone());
        	        
            	//不可重复随机数
                String loginNo = String.valueOf(idWorkByTwitter.nextId());
                //盐
                String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
                //生成密码
                String password = Md5.md5Digest(hardIdRegister.getPassword() + credentialsSalt).toLowerCase();
                
                member.setPassword(password);// 更新密码
            	
                return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
            }
        }
    }
	
	
	/**
     * 忘记密码后重置密码
     *
     * @param mode     0为手机验证，1为邮箱验证
     * @param account  手机或邮箱
     * @param code     验证码
     * @param password 新密码
     * @return
     */
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public MessageResult forgetPassword(@Valid HardIdRegister hardIdRegister,
        BindingResult bindingResult,HttpServletRequest request) throws Exception {
    	log.info("reset-password start");
    	MessageResult result = BindingResultUtil.validate(bindingResult);
   
    	if (result != null) {
    		return result;
    	}
    	
    	String mobilePhone = hardIdRegister.getMobilePhone();
    	String email = hardIdRegister.getEmail();
    	String password = hardIdRegister.getPassword();
    	
    	Member member = null;
    	
    	if (StringUtils.isNotBlank(mobilePhone)&&StringUtils.isNotBlank(email)) {
    		member = memberService.findMemberByMobilePhoneOrEmail(mobilePhone, email);
    	} else if (StringUtils.isNotBlank(mobilePhone)) {
    		member = memberService.findByPhone(mobilePhone);
    	} else if (StringUtils.isNotBlank(email)) {
    		member = memberService.findByEmail(email);
    	} else {
    		return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
    	}
    	
        isTrue(password.length() >= 6 && password.length() <= 20, localeMessageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        
        if (MemberRegisterOriginEnum.HARDID.getSourceType().intValue()!=member.getOrigin().intValue()) {
        	return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        }
        
        //生成密码
        String newPassword = Md5.md5Digest(password + member.getSalt()).toLowerCase();
        member.setPassword(newPassword);
       
        return success();
    }
    
    
    Member getMember(HardIdRegister hardIdRegister) throws Exception {
    	Member member = new Member();
    	
    	//不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
        //生成密码
        String password = Md5.md5Digest(hardIdRegister.getPassword() + credentialsSalt).toLowerCase();
    	
        Location location = new Location();
        Country country = new Country();
        location.setCountry(null);
        country.setZhName(null);
        member.setCountry(null);
        member.setLocation(null);
        
        member.setStatus(CommonStatus.NORMAL);
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        member.setPassword(password);
        member.setEmail(hardIdRegister.getEmail());
        member.setMobilePhone(hardIdRegister.getMobilePhone());
        member.setSalt(credentialsSalt);
        member.setOrigin(MemberRegisterOriginEnum.HARDID.getSourceType());//代表来自应用HardId
        
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
                    log.info("remote call:service={},result={}", serviceName, result);
                    if (result.getStatusCode().value() == 200) {
                        MessageResult mr = result.getBody();
                        log.info("mr={}", mr);
                        if (mr.getCode() == 0) {
                            //返回地址成功，调用持久化
                            String address = (String) mr.getData();
                            wallet.setAddress(address);
                        }
                    }
                }
                catch (Exception e){
                	log.error("call {} failed,error={}",serviceName,e.getMessage());
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
