package ai.turbochain.ipex.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

//使用链接的方法发送国际语言代码
public class MyLocaleResolver  implements LocaleResolver{

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		String lang = request.getParameter("lang");
		Locale locale = Locale.getDefault();
		if(StringUtils.hasText(lang)){
			try{
				String[] split = lang.split("_");
				locale = new Locale(split[0], split[1]);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return locale;
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

	}
}