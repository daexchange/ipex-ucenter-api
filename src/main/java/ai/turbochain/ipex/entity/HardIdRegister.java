package ai.turbochain.ipex.entity;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * @author  
 * @date 2019年8月9日
 */
@Data
public class HardIdRegister {

   // @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    private String email;

   // @NotBlank(message = "{LoginByEmail.password.null}")
   // @Length(min = 6, max = 20, message = "{LoginByEmail.password.length}")
    private String password;

  //  @NotBlank(message = "{LoginByEmail.username.null}")
  //  @Length(min = 3, max = 20, message = "{LoginByEmail.username.length}")
   // private String username;
    
    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+任意数
     * 17+任意数
     * 147
     */
    @Pattern(regexp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(147))\\d{8}$", message = "{LoginByPhone.phone.pattern}")
    /* @NotBlank(message = "{LoginByPhone.phone.null}")*/
    private String mobilePhone;
}
