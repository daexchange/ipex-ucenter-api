package ai.turbochain.ipex.bean;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 
 */
@Data
public class AssetWallet {
 
	private String coinName;
	
    /**
     * 可用余额
     */
    private BigDecimal balance;
    /**
     * 冻结余额
     */
    private BigDecimal frozenBalance;

    /**
     * 待释放总量
     */
    private BigDecimal toReleased;

    /**
     * 充值地址
     */
    private String address;
   
}
