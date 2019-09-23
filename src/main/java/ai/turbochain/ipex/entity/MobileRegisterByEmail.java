package ai.turbochain.ipex.entity;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * @author  
 * @date 2019年8月9日
 */
@Data
public class MobileRegisterByEmail {

    @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    private String email;

    @NotBlank(message = "{LoginByEmail.password.null}")
    @Length(min = 6, max = 20, message = "{LoginByEmail.password.length}")
    private String password;

  //  @NotBlank(message = "{LoginByEmail.username.null}")
  //  @Length(min = 3, max = 20, message = "{LoginByEmail.username.length}")
   // private String username;
}
