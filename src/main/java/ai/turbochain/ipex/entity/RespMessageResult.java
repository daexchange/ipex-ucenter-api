package ai.turbochain.ipex.entity;

import ai.turbochain.ipex.util.MessageResult;
import lombok.Data;

/**
 * @author 未央
 * @create 2019-12-20 14:04
 */
@Data
public class RespMessageResult{

    private int code;
    private String message;
    private Object Data;

    private String totalPage;
    private String totalElement;

    private String totalAssets;

}
