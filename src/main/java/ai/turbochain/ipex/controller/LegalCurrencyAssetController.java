package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.sparkframework.lang.Convert;

import ai.turbochain.ipex.constant.AccountType;
import ai.turbochain.ipex.constant.TransactionType;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.es.ESUtils;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberTransactionService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.service.WalletTransferRecordService;
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
    private WalletTransferRecordService walletTransferRecordService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Value("${gcx.match.max-limit:1000}")
    private double gcxMatchMaxLimit;
    @Value("${gcx.match.each-limit:5}")
    private double gcxMatchEachLimit;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ESUtils esUtils;

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("wallet")
    public MessageResult findWallet(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<MemberLegalCurrencyWallet> wallets = memberLegalCurrencyWalletService.findAllByMemberId(member.getId());
        wallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            if (rate != null) {
                wallet.getCoin().setUsdRate(rate.getUsdRate().doubleValue());
                wallet.getCoin().setCnyRate(rate.getCnyRate().doubleValue());
            } else {
                log.info("unit = {} , rate = null ", wallet.getCoin().getUnit());
            }
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(wallets);
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

    /**
     * 查询所有记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("transaction/all")
    public MessageResult findTransaction(@SessionAttribute(SESSION_MEMBER) AuthMember member, HttpServletRequest request, int pageNo, int pageSize,
                                         @RequestParam(value = "startTime",required = false)  String startTime,
                                         @RequestParam(value = "endTime",required = false)  String endTime,
                                         @RequestParam(value = "symbol",required = false)  String symbol,
                                         @RequestParam(value = "type",required = false)  String type) throws ParseException {
        MessageResult mr = new MessageResult();
        TransactionType transactionType = null;
        if (StringUtils.isNotEmpty(type)) {
            transactionType = TransactionType.valueOfOrdinal(Convert.strToInt(type, 0));
        }
        mr.setCode(0);
        mr.setMessage("success");
        mr.setData(walletTransferRecordService.queryByMember(member.getId(), pageNo, pageSize, transactionType, startTime, endTime,symbol));
        
        return mr;
    }

    @RequestMapping("wallet/{symbol}")
    public MessageResult findWalletBySymbol(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String symbol) {
        MessageResult mr = MessageResult.success("success");
        mr.setData(memberLegalCurrencyWalletService.findByCoinUnitAndMemberId(symbol, member.getId()));
        return mr;
    }

    /**
     * 币种转化(GCC配对GCX,特殊用途，其他项目可以不管)
     *
     * @return
     */
    @RequestMapping("/wallet/coin")
    public MessageResult transformCheck(
    		///{coinId}@PathVariable("coinId") 
    		String coinId,
    		@SessionAttribute(SESSION_MEMBER) AuthMember member) throws Exception {
    	long memberId = member.getId();
    	
    	MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
    	
    	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);

    	Map<String ,Object> data = new HashMap<String ,Object>();
    	
    	data.put("legalCurrencyBalance", memberLegalCurrencyWallet.getBalance());
    	data.put("exchangeBalance", memberWallet.getBalance());
    	
        MessageResult mr = new MessageResult(0, "success");
        mr.setData(data);
        return mr;
    }
   
    
    // 自己划转
    @RequestMapping("/transfer")
    public MessageResult transfer(@SessionAttribute(SESSION_MEMBER) AuthMember member, 
    		AccountType from, AccountType to,
    		String coinId,
    		BigDecimal amount) throws Exception {
    	if(amount.compareTo(BigDecimal.ZERO) <= 0){
            return new MessageResult(500,"划转数量必须大于0");
        }
      /**  String symbol = "GCX";
        
        if(amount.doubleValue() > gcxMatchEachLimit){
            return new MessageResult(500,"单人配对不能超过" + gcxMatchEachLimit);
        }
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(member.getId() != 7 && hour < 9){
            return new MessageResult(500,"每日9:00开放配对");
        }
        if(transactionService.isOverMatchLimit(DateUtil.YYYY_MM_DD.format(new Date()), gcxMatchMaxLimit)){
            return new MessageResult(500,"今日配对额度已售罄");
        }
        BigDecimal matchedAmount = transactionService.findMemberDailyMatch(member.getId(),DateUtil.YYYY_MM_DD.format(new Date()));
        if(matchedAmount.compareTo(new BigDecimal(gcxMatchEachLimit)) >= 0){
            return new MessageResult(500,"您今日配对已达上限" + gcxMatchEachLimit);
        }*/
        //TODO 1.根据用户评分限制划转
        
    	long memberId = member.getId();
    	
    	//TODO 2.划转
        if (AccountType.LegalCurrencyAccount.equals(from)&&// 法币转币币
        		AccountType.ExchangeAccount.equals(to)) {
        	 
        	MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
            
        	if (memberLegalCurrencyWallet.getBalance().compareTo(amount) < 0) {
                return new MessageResult(500, "可划转余额不足");
            }
        	
        	memberLegalCurrencyWalletService.transferDecreaseBalance(memberLegalCurrencyWallet, coinId, memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&// 币币转法币
        		AccountType.LegalCurrencyAccount.equals(to)) {
        	 
        	MemberWallet memberWallet = memberWalletService.getMemberWalletByCoinAndMemberId(coinId, memberId);
            
        	if (memberWallet.getBalance().compareTo(amount) < 0) {
                return new MessageResult(500, "可划转余额不足");
            }
        	
        	memberLegalCurrencyWalletService.transferIncreaseBalance(memberWallet, coinId, memberId, amount);
        } else {
        	return new MessageResult(500,"请重新选择划转账户");
        }
        
        return new MessageResult(0,"success");
    }
    
}
