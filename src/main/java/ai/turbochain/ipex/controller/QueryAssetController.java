package ai.turbochain.ipex.controller;

import ai.turbochain.ipex.constant.TransactionType;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.OtcCoin;
import ai.turbochain.ipex.entity.RespWallet;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberTransactionService;
import ai.turbochain.ipex.service.OtcCoinService;
import ai.turbochain.ipex.system.CoinExchangeFactory;
import ai.turbochain.ipex.util.MessageResult;
import com.netflix.discovery.converters.Auto;
import com.sparkframework.lang.Convert;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static ai.turbochain.ipex.constant.SysConstant.API_HARD_ID_MEMBER;
import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

/**
 * @author 未央
 * @create 2019-12-13 9:47
 */
@RestController
@RequestMapping("/query-asset")
@Slf4j
public class QueryAssetController {

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private MemberLegalCurrencyWalletService memberLegalCurrencyWalletService;

    @Autowired
    private OtcCoinService otcCoinService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    /**
     * 查询所有记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("transaction/all")
    public MessageResult findTransaction(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member, HttpServletRequest request, int pageNo, int pageSize,
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
        mr.setData(transactionService.queryByMember(member.getId(), pageNo, pageSize, transactionType, startTime, endTime,symbol));
        return mr;
    }

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("/wallet")
    public MessageResult findWallet(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member) {
        List<MemberLegalCurrencyWallet> wallets = memberLegalCurrencyWalletService.findAllByMemberId(member.getId());
        List<RespWallet> respWalletList = new ArrayList<>();
        wallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getOtcCoin().getUnit());
            if (rate != null) {
            //    wallet.getCoin().setUsdRate(rate.getUsdRate().doubleValue());
            //    wallet.getCoin().setCnyRate(rate.getCnyRate().doubleValue());
            } else {
                log.info("unit = {} , rate = null ", wallet.getOtcCoin().getUnit());
            }
            OtcCoin otcCoin = otcCoinService.findByUnit(wallet.getOtcCoin().getUnit());
            RespWallet respWallet = new RespWallet();
            respWallet.setWallet(wallet);
            respWallet.setOtcCoin_id(otcCoin.getId());
            System.out.println(otcCoin.toString());
            respWalletList.add(respWallet);
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(respWalletList);
        return mr;
    }

}
