package ai.turbochain.ipex.entity;

import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.entity.Alipay;
import ai.turbochain.ipex.entity.BankInfo;
import ai.turbochain.ipex.entity.WechatPay;
import lombok.Builder;
import lombok.Data;

/**
 * @author GS
 * @date 2018年01月16日
 */
@Builder
@Data
public class MemberAccount {
    private String realName;
    private BooleanEnum bankVerified;
    private BooleanEnum aliVerified;
    private BooleanEnum wechatVerified;
    private BankInfo bankInfo;
    private Alipay alipay;
    private WechatPay wechatPay;
}
