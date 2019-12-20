package ai.turbochain.ipex.entity;

import ai.turbochain.ipex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author 未央
 * @create 2019-12-20 11:36
 */
@Data
public class RespCurrencyWallet {

    private Long id;

    private Long memberId;

    private OtcCoin otcCoin;

    /**
     * 可用余额
     */
    private String balance;
    /**
     * 冻结余额
     */
    private String frozenBalance;

    /**
     * 待释放总量
     */
    private String toReleased;

    private int version;

    /**
     * 钱包是否锁定，0否，1是。锁定后
     */
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;

}
