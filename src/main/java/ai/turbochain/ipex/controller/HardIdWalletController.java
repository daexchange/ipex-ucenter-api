package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.API_HARD_ID_MEMBER;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alibaba.fastjson.JSONObject;

import ai.turbochain.ipex.bean.AssetWallet;
import ai.turbochain.ipex.constant.AccountType;
import ai.turbochain.ipex.entity.ExchangeCoin;
import ai.turbochain.ipex.entity.ExchangeOrder;
import ai.turbochain.ipex.entity.LoanWallet;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.OtcCoin;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.ExchangeCoinService;
import ai.turbochain.ipex.service.ExchangeOrderService;
import ai.turbochain.ipex.service.LoanWalletService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
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
    @Autowired
    private LoanWalletService loanWalletService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    
    
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
       
    	String unit = coinId;
    	long memberId = authMember.getId();
    	
    	Member member = memberService.findOne(memberId);
    	
    	//TODO 1.根据用户评分限制划转
    	//ExangeAssetController.checkMemberTransferToSelf(member,messageSourceService);
    	
    	// 根据coinUnit 查询otcCoin
    	OtcCoin otcCoin = null;
    	
    	if (AccountType.LegalCurrencyAccount.equals(from) || AccountType.LegalCurrencyAccount.equals(to)) {
    		
    		otcCoin = otcCoinService.findByUnit(unit);
    		
    		if (StringUtils.isBlank(coinId)||otcCoin==null) {
        		return new MessageResult(500,"当前币种不存在");
        	}
    	}
    	
    	//TODO 2.划转
        if (AccountType.LegalCurrencyAccount.equals(from)&&AccountType.ExchangeAccount.equals(to)) {
        	// 法币转币币
        	return memberLegalCurrencyWalletService.transferDecreaseBalance(coinId,otcCoin.getId(), memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&AccountType.LegalCurrencyAccount.equals(to)) {
        	// 币币转法币
        	return memberLegalCurrencyWalletService.transferIncreaseBalance(coinId,otcCoin.getId(), memberId, amount);
        } else if (AccountType.ExchangeAccount.equals(from)&&
        		AccountType.LoanAccount.equals(to)) {
        	// 币币转借贷
        	return loanWalletService.transferBalanceCoinToLoan(unit, memberId, amount);
        }else if (AccountType.LoanAccount.equals(from)&&
        		AccountType.ExchangeAccount.equals(to)) {
        	// 借贷转币币
        	return loanWalletService.transferBalanceLoanToCoin(unit, memberId, amount);
        } else if (AccountType.LegalCurrencyAccount.equals(from)&&
        		AccountType.LoanAccount.equals(to)) {
        	// 法币转借贷
        	return loanWalletService.transferBalanceLegalCurrencyToLoan(coinId, otcCoin.getId(), memberId, amount);
        } else if (AccountType.LoanAccount.equals(from)&&
        		AccountType.LegalCurrencyAccount.equals(to)) {
        	// 借贷转法币
        	return loanWalletService.transferBalanceLoanToLegalCurrency(coinId, otcCoin.getId(), memberId, amount);
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
    
    
    /**
        * 会员账户钱包总资产信息
     *
     * @param member
     * @return
     */
    @RequestMapping("/asset")
    public MessageResult Asset(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member, AccountType accountType) {
    	 
    	MessageResult mr = new MessageResult();
    	
    	mr.setCode(0);
        mr.setMessage("success");
    	
    	Map<String, BigDecimal> cnyRateMap = new HashMap<String, BigDecimal>();
    	
    	// 查下所有的币种
    	List<ExchangeCoin> coinList = exchangeCoinService.findAllEnabled();
    	
    	cnyRateMap.put("CNDT", BigDecimal.ONE);
		
    	coinList.forEach(t -> {
    		if ("CNDT".equals(t.getCoinSymbol())) {
    			cnyRateMap.put("CNDT", BigDecimal.ONE);
    		} else {
    			// 查询最新交易数据
    			Page<ExchangeOrder> page = exchangeOrderService.page(t.getSymbol(), 1, 1);
        		
    			List<ExchangeOrder> list = page.getContent();
        		
    			if (list.size()==1) {
    				ExchangeOrder exchangeOrder = list.get(0);
            		
    				cnyRateMap.put(exchangeOrder.getCoinSymbol(), exchangeOrder.getPrice());
        		}
    		}
    	});
    	
    	List<AssetWallet> assetWalletList = new ArrayList<AssetWallet>();
    	
    	if (accountType!=null) {
    		
    		if (AccountType.ExchangeAccount.equals(accountType)) {
    			
    			List<MemberWallet> memberWalletList = memberWalletService.findAllByMemberId(member.getId());
    			
    			memberWalletList.forEach(t->{
    				AssetWallet assetWallet = new AssetWallet();
    				String coinName = t.getCoin().getName();
    				
    				BigDecimal cnyRate = cnyRateMap.get(coinName);
    				
    				if (cnyRate==null) {
    					cnyRate = BigDecimal.ONE;
    				}
    				
        			assetWallet.setAddress(t.getAddress());
        			assetWallet.setBalance(t.getBalance().multiply(cnyRate));
        			assetWallet.setCoinName(coinName);
        			assetWallet.setFrozenBalance(t.getFrozenBalance().multiply(cnyRate));
    			
        			assetWalletList.add(assetWallet);
    			});
    			
    			BigDecimal assetUnion = BigDecimal.ZERO;
    			
    			for (AssetWallet assetWallet : assetWalletList) {
    				assetUnion = assetUnion.add(assetWallet.getFrozenBalance()).add(assetWallet.getBalance());
    			}
    			
    		    Map<String,Object> assetUnionMap = new HashMap<String,Object>();
    			
    		    assetUnionMap.put("assetCnyWalletList",assetWalletList);
    		    assetUnionMap.put("assetCnyUnion",assetUnion);
    		    
    			mr.setData(assetUnionMap);
        		
        		return mr;
    		} else if (AccountType.LoanAccount.equals(accountType)) {
    			
    			List<LoanWallet> loanWalletList = loanWalletService.getByMemberId(member.getId());
    			
    			loanWalletList.forEach(t->{
    				AssetWallet assetWallet = new AssetWallet();
    				
    				String coinName = t.getUnit();
    				
    				BigDecimal cnyRate = cnyRateMap.get(coinName);
    				
    				if (cnyRate==null) {
    					cnyRate = BigDecimal.ONE;
    				}
    				
        			assetWallet.setBalance(t.getBalance().multiply(cnyRate));
        			assetWallet.setCoinName(coinName);
        			assetWallet.setFrozenBalance(t.getFrozenBalance().multiply(cnyRate));
    			
        			assetWalletList.add(assetWallet);
    			});
    			
    			BigDecimal assetUnion = BigDecimal.ZERO;
    			
    			for (AssetWallet assetWallet : assetWalletList) {
    				assetUnion = assetUnion.add(assetWallet.getFrozenBalance()).add(assetWallet.getBalance());
    			}
    			
    		    Map<String,Object> assetUnionMap = new HashMap<String,Object>();
    			
    		    assetUnionMap.put("assetCnyWalletList",assetWalletList);
    		    assetUnionMap.put("assetCnyUnion",assetUnion);
    		    
    			mr.setData(assetUnionMap);
        		
        		return mr;
    		}
    		
    		return mr;
    	} else {
    		List<MemberWallet> memberWalletList = memberWalletService.findAllByMemberId(member.getId());
			
			memberWalletList.forEach(t->{
				AssetWallet assetWallet = new AssetWallet();
				String coinName = t.getCoin().getName();
				
				BigDecimal cnyRate = cnyRateMap.get(coinName);
				
				if (cnyRate==null) {
					cnyRate = BigDecimal.ONE;
				}
				
    			assetWallet.setAddress(t.getAddress());
    			assetWallet.setBalance(t.getBalance().multiply(cnyRate));
    			assetWallet.setCoinName(coinName);
    			assetWallet.setFrozenBalance(t.getFrozenBalance().multiply(cnyRate));
			
    			assetWalletList.add(assetWallet);
			});
			
			List<LoanWallet> loanWalletList = loanWalletService.getByMemberId(member.getId());
			
			for (LoanWallet loanWallet : loanWalletList) {
				String unit = loanWallet.getUnit();
				
				BigDecimal cnyRate = cnyRateMap.get(unit);
				
				if (cnyRate==null) {
					cnyRate = BigDecimal.ONE;
				}
				
				for (AssetWallet assetWallet : assetWalletList) {
					if (assetWallet.getCoinName().equals(unit)) {
						assetWallet.setBalance(assetWallet.getBalance().add(loanWallet.getBalance().multiply(cnyRate)));
						assetWallet.setFrozenBalance(assetWallet.getFrozenBalance().add(loanWallet.getFrozenBalance().multiply(cnyRate)));
						break;
					}
				}
			}
			
			BigDecimal assetUnion = BigDecimal.ZERO;
			
			for (AssetWallet assetWallet : assetWalletList) {
				assetUnion = assetUnion.add(assetWallet.getFrozenBalance()).add(assetWallet.getBalance());
			}
			
		    Map<String,Object> assetUnionMap = new HashMap<String,Object>();
			
		    assetUnionMap.put("assetCnyWalletList",assetWalletList);
		    assetUnionMap.put("assetCnyUnion",assetUnion);
		    
			mr.setData(assetUnionMap);
    		
			return mr;
    	}
    }
    
}