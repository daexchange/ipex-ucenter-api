package ai.turbochain.ipex.controller;

import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.querydsl.core.types.dsl.BooleanExpression;

import ai.turbochain.ipex.entity.LegalWalletWithdrawModel;
import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.constant.WithdrawStatus;
import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.LegalWalletWithdraw;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.QLegalWalletWithdraw;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.LegalWalletWithdrawService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.util.BigDecimalUtils;
import ai.turbochain.ipex.util.BindingResultUtil;
import ai.turbochain.ipex.util.MessageResult;

/**
 * 提现
 */
@RestController
@RequestMapping("legal-wallet-withdraw")
public class LegalWalletWithdrawController extends BaseController {
    @Autowired
    private LegalWalletWithdrawService legalWalletWithdrawService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService walletService;

    @GetMapping()
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(value = "state", required = false) WithdrawStatus status,
            @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        BooleanExpression eq = QLegalWalletWithdraw.legalWalletWithdraw.member.id.eq(user.getId());
        if (status != null) {
            eq.and(QLegalWalletWithdraw.legalWalletWithdraw.status.eq(status));
        }
        Page<LegalWalletWithdraw> page = legalWalletWithdrawService.findAll(eq, pageModel);
        return success(page);
    }

    @PostMapping()
    public MessageResult post(
            LegalWalletWithdrawModel model,
            BindingResult bindingResult,
            @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        // 合法币种
        Coin coin = coinService.findByUnit(model.getUnit());
        Assert.notNull(coin, "validate coin name!");
        Assert.isTrue(coin.getHasLegal(), "validate coin name!");
        //用户提现的币种钱包
        MemberWallet wallet = walletService.findOneByCoinNameAndMemberId(coin.getName(), user.getId());
        Assert.notNull(wallet, "wallet null!");
        Assert.isTrue(BigDecimalUtils.compare(wallet.getBalance(), model.getAmount()), "insufficient balance!");
        //提现人
        Member member = memberService.findOne(user.getId());
        Assert.notNull(member, "validate login user!");
        //创建 提现
        LegalWalletWithdraw legalWalletWithdraw = new LegalWalletWithdraw();
        legalWalletWithdraw.setMember(member);
        legalWalletWithdraw.setCoin(coin);
        legalWalletWithdraw.setAmount(model.getAmount());
        legalWalletWithdraw.setPayMode(model.getPayMode());
        legalWalletWithdraw.setStatus(WithdrawStatus.PROCESSING);
        legalWalletWithdraw.setRemark(model.getRemark());
        //提现操作
        legalWalletWithdrawService.withdraw(wallet, legalWalletWithdraw);
        return success();
    }

    @GetMapping("{id}")
    public MessageResult detail(
            @PathVariable Long id,
            @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        LegalWalletWithdraw one = legalWalletWithdrawService.findDetailWeb(id, user.getId());
        Assert.notNull(one, "validate id!");
        return success(one);
    }

}
