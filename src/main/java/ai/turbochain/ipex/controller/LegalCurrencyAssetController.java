package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.constant.AccountType;
import ai.turbochain.ipex.constant.TransactionType;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberTransactionService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.system.CoinExchangeFactory;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/legal-currency/asset")
@Slf4j
public class LegalCurrencyAssetController {
    @Autowired
    private MemberLegalCurrencyWalletService memberLegalCurrencyWalletService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LocaleMessageSourceService messageSourceService;
    
    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("/wallet")
    public MessageResult findWallet(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<MemberLegalCurrencyWallet> legalCurrencyWallets = memberLegalCurrencyWalletService.findAllByMemberId(member.getId());
        legalCurrencyWallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getOtcCoin().getUnit());
            if (rate != null) {
               // wallet.getOtcCoin().setUsdRate(rate.getUsdRate().doubleValue());
             //   wallet.getCoin().setCnyRate(rate.getCnyRate().doubleValue());
            } else {
                log.info("unit = {} , rate = null ", wallet.getOtcCoin().getUnit());
            }
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(legalCurrencyWallets);
        return mr;
    }

    /**
     * 查询特定类型的记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @param type
     * @return
     */
    @RequestMapping("transaction")
    public MessageResult findTransaction(@SessionAttribute(SESSION_MEMBER) AuthMember member, int pageNo, int pageSize, TransactionType type) {
        MessageResult mr = new MessageResult();
        mr.setData(transactionService.queryByMember(member.getId(), pageNo, pageSize, type));
        mr.setCode(0);
        mr.setMessage("success");
        return mr;
    }

    @RequestMapping("wallet/{symbol}")
    public MessageResult findWalletBySymbol(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String symbol) {
        MessageResult mr = MessageResult.success("success");
        mr.setData(memberLegalCurrencyWalletService.findByOtcCoinUnitAndMemberId(symbol, member.getId()));
        return mr;
    }
   
    
    /**
     * 资金划转
     *
     * @return
     */
    @RequestMapping("/transfer")
    public MessageResult transfer(@SessionAttribute(SESSION_MEMBER) AuthMember authMember, 
    		AccountType from, AccountType to, String coinId, BigDecimal amount) throws Exception {
    	
    	if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500,"划转数量必须大于0");
        }
       
    	long memberId = authMember.getId();
    	
    	Member member = memberService.findOne(memberId);
    	
    	//TODO 1.根据用户评分限制划转
    	ExangeAssetController.checkMemberTransferToSelf(member,messageSourceService);
    	
    	//TODO 2.划转
        if (AccountType.LegalCurrencyAccount.equals(from)&&// 法币转币币
        		AccountType.ExchangeAccount.equals(to)) {
        	return memberLegalCurrencyWalletService.transferDecreaseBalance(coinId, memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&// 币币转法币
        		AccountType.LegalCurrencyAccount.equals(to)) {
        	return memberLegalCurrencyWalletService.transferIncreaseBalance(coinId, memberId, amount);
        } else {
        	return new MessageResult(500,"请重新选择划转账户");
        }
        
    }
    
   
    /**
     * 根据币种获取账户信息
     * @param coinId
     * @return
     */
    @RequestMapping("/coin")
    public MessageResult transformCheck(
    		@SessionAttribute(SESSION_MEMBER) AuthMember member,String coinId) {
    	
    	long memberId = member.getId();
    	
    	// 法币账户
    	MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
    	
    	// 币币账户
    	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);

        MessageResult mr = new MessageResult(0, "success");
       
        Map<String ,Object> data = new HashMap<String ,Object>();
    	
    	data.put("legalCurrencyBalance", memberLegalCurrencyWallet.getBalance());
    	data.put("exchangeBalance", memberWallet.getBalance());
        
    	mr.setData(data);
        
        return mr;
    }
}
