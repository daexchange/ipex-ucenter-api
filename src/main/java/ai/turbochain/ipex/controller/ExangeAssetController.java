package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;

import java.math.BigDecimal;

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
import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.ExangeService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.util.Md5;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/exange/asset")
@Slf4j
public class ExangeAssetController {
   
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
    
    boolean checkJyPassword(Member memberFrom, String jyPassword) throws Exception {
       
    	hasText(jyPassword, messageSourceService.getMessage("MISSING_JYPASSWORD"));
    	
    	String mbPassword = memberFrom.getJyPassword();
         
        Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + memberFrom.getSalt()).toLowerCase().equals(mbPassword), messageSourceService.getMessage("ERROR_JYPASSWORD"));
       
        return true;
    }
    
    
    /**
     * 转账
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping("/transfer")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transfer(@SessionAttribute(SESSION_MEMBER) AuthMember user, 
    		String coinId, String email,
            BigDecimal amount, BigDecimal fee, @RequestParam("code") String code, String jyPassword) throws Exception {
    	
    	String key = SysConstant.EMAIL_EXANGE_TRANSFER_PREFIX  + user.getEmail();
    	
    	Object cache = redisTemplate.opsForValue().get(key);
        
    	if (null == cache || !code.equals(cache.toString())) {
            return MessageResult.error(messageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
        }
        
    	Long memberId = user.getId();
    	
    	Member memberFrom = memberService.findOne(memberId);

    	//校验交易密码
        checkJyPassword(memberFrom, jyPassword);
       
    	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
        
    	if (memberWallet.getBalance().compareTo(amount) < 0) {
            return new MessageResult(500, "余额不足");
        }
    	
    	//TODO 2.扣减手续费
    	
    	Member memberTo = memberService.findByEmail(email);
    	
    	if (memberTo==null) {
    		// TODO 国际化
    		return MessageResult.error("该邮件尚未注册会员！");
    	}

    	//TODO 1.限制转账
    	checkMemberTransferLimit(memberTo,messageSourceService);
    	checkMemberTransferLimit(memberFrom,messageSourceService);
    	
    	MessageResult messageResult = exangeService.transferToOther(memberWallet,coinId, memberId, memberTo.getId(), amount);
    	
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
    
}
