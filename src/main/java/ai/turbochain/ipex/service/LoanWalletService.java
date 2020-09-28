package ai.turbochain.ipex.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import ai.turbochain.ipex.dao.CoinDao;
import ai.turbochain.ipex.dao.LoanWalletDao;
import ai.turbochain.ipex.dao.MemberLegalCurrencyWalletDao;
import ai.turbochain.ipex.dao.MemberWalletDao;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.LoanWallet;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.TransferSelfRecord;
import ai.turbochain.ipex.service.Base.BaseService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoanWalletService extends BaseService<LoanWallet> {
    @Autowired
    private MemberLegalCurrencyWalletDao memberLegalCurrencyWalletDao;
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private TransferSelfRecordService transferSelfRecordService;
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private LoanWalletDao loanWalletDao;
    
    public static Integer COIN_TO_LOAN_STATUS = 3;
    public static Integer LOAN_TO_COIN_STATUS = 4;
    public static Integer LegalCurrency_TO_LOAN_STATUS = 5;
    public static Integer LOAN_TO_LegalCurrency_STATUS = 6;
    
   // type=0 币币转入法币账户 type=1 币币转出法币账户 
    // 3：币币转到借贷 4：借贷转到币币
 // 5：法币转到借贷 6：借贷转到法币
    
    /**
     * 币币转到借贷
     *
     * @param wallet
     * @param amount
     * @return
     * @throws Exception 
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transferBalanceCoinToLoan(String coinUnit, Long memberId, BigDecimal amount) throws Exception {

    	MemberWallet memberWallet = memberWalletDao.getLockMemberWalletByCoinUnitAndMemberId(coinUnit, memberId);
        
    	if (memberWallet.getBalance().compareTo(amount) ==-1) {
            return new MessageResult(500, "可划转余额不足");
        }
    	
    	LoanWallet loanWallet = loanWalletDao.getLockLoanWalletByCoinUnitAndMemberId(coinUnit, memberId);

    	if (loanWallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
       
        int result = loanWalletDao.transferIncreaseBalance(loanWallet.getId(), amount,loanWallet.getBalance(),loanWallet.getVersion());
        
        if (result > 0) {
    	    result =  memberWalletDao.transferDecreaseBalance(memberWallet.getId(), memberId, amount, memberWallet.getBalance());
           
            if (result > 0) {
                 TransferSelfRecord transferSelfRecord = new TransferSelfRecord();
                 
                 Coin coin = coinDao.findByUnit(coinUnit);
         		 
                 transferSelfRecord.setCoin(coin);
               //  transferSelfRecord.setLegalcurrencyId(memberLegalCurrencyWallet.getId());
                 transferSelfRecord.setWalletId(memberWallet.getId());
                 transferSelfRecord.setMemberId(memberId);
                 transferSelfRecord.setType(COIN_TO_LOAN_STATUS);
                 transferSelfRecord.setStatus(1);
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setArrivedAmount(amount);
                 transferSelfRecord.setFee(BigDecimal.ZERO);
                 
                 transferSelfRecordService.save(transferSelfRecord);
                 
                //增加记录
                return new MessageResult(0, "success");
            } else {
            	throw new Exception("转账失败！");
            }
        } else {
            return new MessageResult(500, "recharge failed");
        }
    }
    
    
    /**
     * 借贷到币币资金划转
     *
     * @param wallet
     * @param amount
     * @return
     * @throws Exception 
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageResult transferBalanceLoanToCoin(String coinUnit, Long memberId, BigDecimal amount) throws Exception {
    	
    	LoanWallet loanWallet = loanWalletDao.getLockLoanWalletByCoinUnitAndMemberId(coinUnit, memberId);

    	if (loanWallet.getBalance().compareTo(amount) ==-1) {
            return new MessageResult(500, "可划转余额不足");
        }
    	
    	MemberWallet memberWallet = memberWalletDao.getLockMemberWalletByCoinUnitAndMemberId(coinUnit, memberId);

    	// 账户扣减
        int result = loanWalletDao.transferDecreaseBalance(loanWallet.getId(), amount,loanWallet.getBalance(),loanWallet.getVersion());
       
        if (result > 0) {
        	 
        	// 币币账户增加
        	result = memberWalletDao.transferIncreaseBalance(memberWallet.getId(), memberId, amount, memberWallet.getBalance());
        	
        	if (result > 0) {
        		TransferSelfRecord transferSelfRecord = new TransferSelfRecord();
                 
        		 Coin coin = coinDao.findByUnit(coinUnit);
        		
        		 transferSelfRecord.setCoin(coin);
                // transferSelfRecord.setLegalcurrencyId(memberLegalCurrencyWallet.getId());
                 transferSelfRecord.setWalletId(memberWallet.getId());
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setMemberId(memberId);
                 transferSelfRecord.setType(LOAN_TO_COIN_STATUS);
                 transferSelfRecord.setStatus(1);
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setArrivedAmount(amount);
                 transferSelfRecord.setFee(BigDecimal.ZERO);
                 
               //增加记录
                transferSelfRecordService.save(transferSelfRecord);
                 
                return new MessageResult(0, "success");
        	} else {
        		throw new Exception("划转失败！");
        	}
        } else {
            return new MessageResult(500, "recharge failed");
        }
    }
  
    
    
    /**
     * 法币转到借贷
     *
     * @param wallet
     * @param amount
     * @return
     * @throws Exception 
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transferBalanceLegalCurrencyToLoan(String coinUnit,Long otcCoinId, Long memberId, BigDecimal amount) throws Exception {

    	MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletDao.getLockMemberWalletByOtcCoinIdAndMemberId(otcCoinId, memberId);
        
    	if (memberLegalCurrencyWallet.getBalance().compareTo(amount) ==-1) {
            return new MessageResult(500, "可划转余额不足");
        }
    	
    	LoanWallet loanWallet = loanWalletDao.getLockLoanWalletByCoinUnitAndMemberId(coinUnit, memberId);

    	if (loanWallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
       
        int result = loanWalletDao.transferIncreaseBalance(loanWallet.getId(), amount,loanWallet.getBalance(),loanWallet.getVersion());
        
        if (result > 0) {
    	    result =  memberLegalCurrencyWalletDao.transferDecreaseBalance(memberLegalCurrencyWallet.getId(), amount, memberLegalCurrencyWallet.getBalance());
           
            if (result > 0) {
                 TransferSelfRecord transferSelfRecord = new TransferSelfRecord();
                 
                 Coin coin = coinDao.findByUnit(coinUnit);
         		 
                 transferSelfRecord.setCoin(coin);
                 transferSelfRecord.setLegalcurrencyId(memberLegalCurrencyWallet.getId());
                 //transferSelfRecord.setWalletId(memberWallet.getId());
                 transferSelfRecord.setMemberId(memberId);
                 transferSelfRecord.setType(LegalCurrency_TO_LOAN_STATUS);
                 transferSelfRecord.setStatus(1);
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setArrivedAmount(amount);
                 transferSelfRecord.setFee(BigDecimal.ZERO);
                 
                 transferSelfRecordService.save(transferSelfRecord);
                 
                //增加记录
                return new MessageResult(0, "success");
            } else {
            	throw new Exception("转账失败！");
            }
        } else {
            return new MessageResult(500, "recharge failed");
        }
    }
    
    
    /**
     * 借贷到法币币资金划转
     *
     * @param wallet
     * @param amount
     * @return
     * @throws Exception 
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageResult transferBalanceLoanToLegalCurrency(String coinUnit,Long otcCoinId, Long memberId, BigDecimal amount) throws Exception {
    	
    	LoanWallet loanWallet = loanWalletDao.getLockLoanWalletByCoinUnitAndMemberId(coinUnit, memberId);

    	if (loanWallet.getBalance().compareTo(amount) ==-1) {
            return new MessageResult(500, "可划转余额不足");
        }
    	
    	MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletDao.getLockMemberWalletByOtcCoinIdAndMemberId(otcCoinId, memberId);
        
    	// 账户扣减
        int result = loanWalletDao.transferDecreaseBalance(loanWallet.getId(), amount,loanWallet.getBalance(),loanWallet.getVersion());
       
        if (result > 0) {
        	 
        	// 账户增加
        	result = memberLegalCurrencyWalletDao.transferIncreaseBalance(memberLegalCurrencyWallet.getId(), amount, memberLegalCurrencyWallet.getBalance());
        	
        	if (result > 0) {
        		TransferSelfRecord transferSelfRecord = new TransferSelfRecord();
                 
        		 Coin coin = coinDao.findByUnit(coinUnit);
        		
        		 transferSelfRecord.setCoin(coin);
                 transferSelfRecord.setLegalcurrencyId(memberLegalCurrencyWallet.getId());
                // transferSelfRecord.setWalletId(memberWallet.getId());
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setMemberId(memberId);
                 transferSelfRecord.setType(LOAN_TO_LegalCurrency_STATUS);
                 transferSelfRecord.setStatus(1);
                 transferSelfRecord.setTotalAmount(amount);
                 transferSelfRecord.setArrivedAmount(amount);
                 transferSelfRecord.setFee(BigDecimal.ZERO);
                 
               //增加记录
                transferSelfRecordService.save(transferSelfRecord);
                 
                return new MessageResult(0, "success");
        	} else {
        		throw new Exception("划转失败！");
        	}
        } else {
            return new MessageResult(500, "recharge failed");
        }
    }
    
    public List<LoanWallet> getByMemberId(long memberId){
    	return loanWalletDao.getByMemberId(memberId);
    }
}
