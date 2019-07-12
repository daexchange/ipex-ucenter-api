package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

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
    
    boolean checkJyPassword(Long memberId, String jyPassword) throws Exception {
       
    	hasText(jyPassword, messageSourceService.getMessage("MISSING_JYPASSWORD"));

    	Member member = memberService.findOne(memberId);
    	
    	String mbPassword = member.getJyPassword();
         
         Assert.hasText(mbPassword, messageSourceService.getMessage("NO_SET_JYPASSWORD"));
         Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword), messageSourceService.getMessage("ERROR_JYPASSWORD"));
       
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
    	
        checkJyPassword(memberId, jyPassword);
       
    	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
        
    	if (memberWallet.getBalance().compareTo(amount) < 0) {
            return new MessageResult(500, "余额不足");
        }
    	//TODO 1.限制转账
    	//TODO 2.扣减手续费
    	
    	// TODO　格式验证
    	
    	Member memberTo = memberService.findByEmail(email);
    	
    	if (memberTo==null) {
    		// TODO 国际化
    		return MessageResult.error("该邮件尚未注册会员！");
    	}
    	
    	exangeService.transferToOther(
    			memberWallet,coinId, memberId, memberTo.getId(), amount);
    	
    	return new MessageResult(0,"success");
    }
}
