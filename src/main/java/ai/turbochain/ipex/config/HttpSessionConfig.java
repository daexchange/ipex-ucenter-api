package ai.turbochain.ipex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

import ai.turbochain.ipex.ext.SmartHttpSessionStrategy;

/**
 * 	一个小时过期
 *  分布式方式
 * @author fly
 *
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class HttpSessionConfig {
	 
	 @Bean
	 public HttpSessionStrategy httpSessionStrategy(){
		 HeaderHttpSessionStrategy headerSession = new HeaderHttpSessionStrategy();
		 CookieHttpSessionStrategy cookieSession = new CookieHttpSessionStrategy();
		 headerSession.setHeaderName("x-auth-token");
		 return new SmartHttpSessionStrategy(cookieSession, headerSession);
	 }
}
