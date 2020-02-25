package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.API_HARD_ID_MEMBER;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alibaba.fastjson.JSONObject;

import ai.turbochain.ipex.constant.AccountType;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.OtcCoin;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.OtcCoinService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/hard-id/wallet")
@Slf4j
public class HardIdWalletController {
    @Autowired
    private MemberLegalCurrencyWalletService memberLegalCurrencyWalletService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    /**
     * 资金划转
     *
     * @return
     */
    @RequestMapping("/transfer")
    public MessageResult transfer(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember authMember, 
    		AccountType from, AccountType to, String coinId, BigDecimal amount) throws Exception {
    	
    	if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500,"划转数量必须大于0");
        }
       
    	long memberId = authMember.getId();
    	
    	Member member = memberService.findOne(memberId);
    	String unit = coinId;
    	
    	// 根据coinUnit 查询otcCoin
    	OtcCoin otcCoin = otcCoinService.findByUnit(unit);
    	
    	if (StringUtils.isBlank(coinId)||otcCoin==null) {
    		return new MessageResult(500,"当前币种不存在");
    	}
    	
    	//TODO 1.根据用户评分限制划转
    	ExangeAssetController.checkMemberTransferToSelf(member,messageSourceService);
    	
    	//TODO 2.划转
        if (AccountType.LegalCurrencyAccount.equals(from)&&AccountType.ExchangeAccount.equals(to)) {
        	// 法币转币币
        	return memberLegalCurrencyWalletService.transferDecreaseBalance(coinId,otcCoin.getId(), memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&AccountType.LegalCurrencyAccount.equals(to)) {
        	// 币币转法币
        	return memberLegalCurrencyWalletService.transferIncreaseBalance(coinId,otcCoin.getId(), memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&// 币币转借贷
        		AccountType.LoanAccount.equals(to)) {
        	return null;
        } else if (AccountType.LegalCurrencyAccount.equals(from)&&// 法币转借贷
        		AccountType.LoanAccount.equals(to)) {
        	return null;
        } else {
        	return new MessageResult(500,"请重新选择划转账户");
        }
    }
    
    
    @RequestMapping("/reset-address")
    public MessageResult resetWalletAddress(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member, String unit) {
        try {
        	if (StringUtils.isBlank(unit)) {
                return new MessageResult(500,"请设置币种类型！");
            }
        	
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            kafkaTemplate.send("reset-member-address", unit, json.toJSONString());
            return MessageResult.success("提交成功");
        } catch (Exception e) {
            return MessageResult.error("未知异常");
        }
    }
}