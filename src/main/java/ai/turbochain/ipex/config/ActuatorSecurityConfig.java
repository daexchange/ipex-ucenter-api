package ai.turbochain.ipex.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 	用于定义配置类，可替换xml配置文件
 * @author fly
 *
 */
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	Environment env;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String contextPath = env.getProperty("management.context-path");
		if (StringUtils.isEmpty(contextPath)) {
			contextPath = "";
		}
		http.csrf().disable();
		http.authorizeRequests().antMatchers("/**" + contextPath + "/**").authenticated().anyRequest().permitAll().and()
				.httpBasic();
	}
}