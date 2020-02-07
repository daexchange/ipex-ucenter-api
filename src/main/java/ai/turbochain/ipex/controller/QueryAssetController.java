package ai.turbochain.ipex.controller;

import ai.turbochain.ipex.constant.RealNameStatus;
import ai.turbochain.ipex.constant.SysConstant;

import static ai.turbochain.ipex.constant.SysConstant.API_HARD_ID_MEMBER;
import static ai.turbochain.ipex.util.MessageResult.error;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ai.turbochain.ipex.entity.*;
import ai.turbochain.ipex.service.*;
import com.netflix.discovery.converters.Auto;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.sparkframework.lang.Convert;

import ai.turbochain.ipex.constant.TransactionType;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.system.CoinExchangeFactory;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberService memberService;

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
                                         @RequestParam(value = "startTime", required = false) String startTime,
                                         @RequestParam(value = "endTime", required = false) String endTime,
                                         @RequestParam(value = "symbol", required = false) String symbol,
                                         @RequestParam(value = "type", required = false) String type) throws ParseException {
        MessageResult mr = new MessageResult();
        TransactionType transactionType = null;
        if (StringUtils.isNotEmpty(type)) {
            transactionType = TransactionType.valueOfOrdinal(Convert.strToInt(type, 0));
        }
        mr.setCode(0);
        mr.setMessage("success");
        mr.setData(transactionService.queryByMember(member.getId(), pageNo, pageSize, transactionType, startTime, endTime, symbol));
        return mr;
    }

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("/wallet")
    public RespMessageResult findWallet(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member) {
        List<MemberLegalCurrencyWallet> wallets = memberLegalCurrencyWalletService.findAllByMemberId(member.getId());
        List<RespWallet> respWalletList = new ArrayList<>();

//        List<String> totalAssets = new ArrayList<>();
//        totalAssets.add("0");
        String[] totalAssets = {"0"};

        wallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getOtcCoin().getUnit());
            if (rate != null) {
                //    wallet.getCoin().setUsdRate(rate.getUsdRate().doubleValue());
                //    wallet.getCoin().setCnyRate(rate.getCnyRate().doubleValue());
            } else {
                log.info("unit = {} , rate = null ", wallet.getOtcCoin().getUnit());
            }


            RespCurrencyWallet currencyWallet = new RespCurrencyWallet();
            currencyWallet.setId(wallet.getId());
            currencyWallet.setMemberId(wallet.getMemberId());
            currencyWallet.setOtcCoin(wallet.getOtcCoin());
            if (wallet.getBalance() != null) {
                currencyWallet.setBalance(wallet.getBalance().toPlainString());
            }
            if (wallet.getFrozenBalance() != null) {
                currencyWallet.setFrozenBalance(wallet.getFrozenBalance().toPlainString());
            }
            if (wallet.getToReleased() != null) {
                currencyWallet.setToReleased(wallet.getToReleased().toPlainString());
            }
            currencyWallet.setVersion(wallet.getVersion());

            OtcCoin otcCoin = otcCoinService.findByUnit(wallet.getOtcCoin().getUnit());
            RespWallet respWallet = new RespWallet();
            respWallet.setWallet(currencyWallet);
            respWallet.setOtcCoin_id(otcCoin.getId());
            System.out.println(otcCoin.toString());
            respWalletList.add(respWallet);

            Object bondValue = coinCnyRateInvoking(wallet.getOtcCoin().getUnit());
            if (bondValue == null) {
                bondValue = 1;
            }

            BigDecimal cnyRate = new BigDecimal(bondValue.toString());
            BigDecimal currentCoinAsset = wallet.getBalance().add(wallet.getFrozenBalance()).multiply(cnyRate);
            BigDecimal coinAsset = new BigDecimal(totalAssets[0]);
            totalAssets[0] = coinAsset.add(currentCoinAsset).toString();


        });
//        List list = new ArrayList();
        Map map = new HashMap();
        map.put("totalAssets", totalAssets[0]);
//        list.add(respWalletList);
//        list.add(map);
//        RespMessageResult mr = RespMessageResult.success("success");
//        mr.setData(respWalletList);
        RespMessageResult mr = new RespMessageResult();
        mr.setData(respWalletList);
        mr.setCode(0);
        mr.setMessage("SUCCESS");
        mr.setTotalPage(null);
        mr.setTotalElement(null);
        mr.setTotalAssets(totalAssets[0]);
        return mr;
    }

    public Object coinCnyRateInvoking(String symbol) {
        String key = SysConstant.DIGITAL_CURRENCY_MARKET_PREFIX + symbol;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object bondvalue = valueOperations.get(key);
        if (bondvalue == null) {
            log.info(symbol + ">>>>>>缓存中无利率转换数据>>>>>");
        } else {
            log.info(symbol + "缓存中利率转换数据为：" + bondvalue);
        }

        return bondvalue;
    }

    /**
     * 数字货币兑人民币比率
     *
     * @param
     * @return
     */
    @GetMapping("/cny-rate")
    public MessageResult CoinCnyRate(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member) {
        List<MemberLegalCurrencyWallet> wallets = memberLegalCurrencyWalletService.findAllByMemberId(member.getId());

        Map<String, Object> cnyMap = new HashMap<>();
        wallets.forEach(wallet -> {
            String key = SysConstant.DIGITAL_CURRENCY_MARKET_PREFIX + wallet.getOtcCoin().getUnit();
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object bondvalue = valueOperations.get(key);
            if (bondvalue == null) {
                log.info(wallet.getOtcCoin().getUnit() + ">>>>>>缓存中无利率转换数据>>>>>");
            } else {
                log.info(wallet.getOtcCoin().getUnit() + "缓存中利率转换数据为：" + bondvalue);
            }
            cnyMap.put(wallet.getOtcCoin().getUnit(), bondvalue);
        });

        //返回的价格数据中加入USDT的价格
        String keyU = SysConstant.DIGITAL_CURRENCY_MARKET_PREFIX + "USDT";
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object bondvalueU = valueOperations.get(keyU);
        if (bondvalueU == null) {
            log.info("USDT" + ">>>>>>缓存中无利率转换数据>>>>>");
        } else {
            log.info("USDT" + "缓存中利率转换数据为：" + bondvalueU);
        }
        cnyMap.put("USDT", bondvalueU);

        return success(cnyMap);
    }

    protected MessageResult success(Object obj) {
        MessageResult mr = new MessageResult(0, "SUCCESS");
        mr.setData(obj);
        return mr;
    }


    /**
     * 地址信息
     *
     * @param member
     * @return
     */
    @RequestMapping("/address")
    public MessageResult getAddress(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember member, String coinUnit) {
        if (StringUtils.isBlank(coinUnit)) {
            return error("请输入币种单位");
        }
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(coinUnit, member.getId());

        if (wallet == null) {
            return error("请检查钱包是否正常");
        }
        MessageResult mr = MessageResult.success("success");
        mr.setData(wallet.getAddress());
        return mr;
    }

    @RequestMapping("/changeStatus")
    public MessageResult changeMemberRealNameStatus(Long id, String code) {
        Member member = memberService.findOne(id);
        if (code.equals("2546")) {
            member.setRealNameStatus(RealNameStatus.AUDITING);
        } else if (code.equals("2547")) {
            member.setRealNameStatus(RealNameStatus.VERIFIED);
        }
        memberService.save(member);
        return MessageResult.success();
    }
}
