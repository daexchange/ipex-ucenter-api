package ai.turbochain.ipex.controller;

import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;
import static ai.turbochain.ipex.util.MessageResult.error;
import static ai.turbochain.ipex.util.MessageResult.success;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.util.BigDecimalUtils;
import ai.turbochain.ipex.util.DateUtil;
import ai.turbochain.ipex.util.GeneratorUtil;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.util.ValidateUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/email")
public class EmailController {

	@Value("${spring.mail.username}")
	private String from;
	@Value("${spark.system.host}")
	private String host;
	@Value("${spark.system.name}")
	private String company;

	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private RedisTemplate redisTemplate;
	@Resource
	private LocaleMessageSourceService localeMessageSourceService;
	@Autowired
	private MemberService memberService;

	/**
	 * 注册验证码发送
	 *
	 * @return
	 */
	@PostMapping("/code")
	public MessageResult sendCheckCode(String email, String country) throws Exception {
		String errorMsg = "请输入正确的邮箱地址";
		if (email == null) {
			return error(errorMsg);
		}
		Assert.isTrue(ValidateUtil.isEmail(email), errorMsg);

//        isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
		MessageResult result;
		// 判断是否已经注册
		if (memberService.emailIsExist(email)) {
			String msg = localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND");
			result = MessageResult.exist(msg);
		} else {
			String subjcet = "[IPEX]邮件验证码";
			result = sendEmailCodeMethod(SysConstant.EMAIL_REG_CODE_PREFIX, email, subjcet);
		}

		return result;
	}

	/**
	 * 修改登录密码
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/update/password/code", method = RequestMethod.POST)
	public MessageResult updatePasswordCode(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		String subjcet = "[IPEX]邮件验证码";
		MessageResult result = this.sendEmailCodeMethod(SysConstant.EMAIL_UPDATE_PASSWORD_PREFIX, member.getEmail(),
				subjcet);
		return result;
	}

	/**
	 * 重置资金密码
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/transaction/code", method = RequestMethod.POST)
	public MessageResult sendTransactionCode(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		String subjcet = "[IPEX]邮件验证码";
		MessageResult result = sendEmailCodeMethod(SysConstant.EMAIL_RESET_TRANS_CODE_PREFIX, member.getEmail(),
				subjcet);
		return result;
	}

	/**
	 * 登录之前发送邮箱验证码
	 * 
	 * @param email
	 * @return
	 */
	@PostMapping("/login/code")
	public MessageResult sendLoginCode(String email) {
		String errorMsg = "请输入正确的邮箱地址";
		if (email == null && ValidateUtil.isEmail(email)) {
			return error(errorMsg);
		}
		String subjcet = "[IPEX]邮件验证码";
		MessageResult result = sendEmailCodeMethod(SysConstant.EMAIL_LOGIN_CODE_PREFIX, email, subjcet);
		return result;
	}

	@PostMapping("/login/success/code")
	public MessageResult sendLoginSuccessCode(String email) {
		String errorMsg = "请输入正确的邮箱地址";
		if (email == null && ValidateUtil.isEmail(email)) {
			return error(errorMsg);
		}
		String subjcet = "[IPEX]登录成功";
		MessageResult result = sendLoginSuccess(email, subjcet);
		return result;
	}

	public MessageResult sendLoginSuccess(String email, String subject) {
		Assert.hasText(email, localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));

		MessageResult result;

		String timeUTC = DateUtil.getUTCTimeStr();

		Map<String, Object> model = new HashMap<>(16);

		model.put("timeUTC", timeUTC);

		result = this.sentCode(email, "loginSuccess.ftl", subject, model);
		if (result.getCode() == 0) {
		}
		return result;
	}

	public MessageResult sendEmailCodeMethod(String prefix, String email, String subject) {
		Assert.hasText(email, localeMessageSourceService.getMessage("NOT_BIND_EMAIL"));
		ValueOperations valueOperations = redisTemplate.opsForValue();
		String key = prefix + email;
		Object code = valueOperations.get(key);
		if (code != null) {
			// 判断如果请求间隔小于一分钟则请求失败
			if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(key + "Time"))),
					BigDecimal.ONE)) {
				return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
			}
		}
		MessageResult result;
		String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
		String timeUTC = DateUtil.getUTCTimeStr();

		Map<String, Object> model = new HashMap<>(16);

		model.put("code", randomCode);
		model.put("title", "邮件验证码");
		model.put("timeUTC", timeUTC);

		result = this.sentCode(email, "registerCodeEmail.ftl", subject, model);
		if (result.getCode() == 0) {
			valueOperations.getOperations().delete(key);
			valueOperations.getOperations().delete(key + "Time");
			// 缓存验证码
			valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
			valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
		}
		return result;
	}

	@Async
	public MessageResult sentCode(String email, String templateName, String subject, Map<String, Object> model) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = null;
			helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(from);
			helper.setTo(email);
			helper.setSubject(subject);
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
			cfg.setClassForTemplateLoading(this.getClass(), "/templates");
			Template template = cfg.getTemplate(templateName);
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			helper.setText(html, true);
			// 发送邮件
			javaMailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
			return error(localeMessageSourceService.getMessage("SEND_FAILED"));
		}
		return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
	}

	/**
	 * 忘记密码，验证码
	 */
	@RequestMapping(value = "/reset/code", method = RequestMethod.POST)
	public MessageResult resetPasswordCode(String account) throws Exception {
		Member member = memberService.findByEmail(account);

		Assert.notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));

		String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
		String timeUTC = DateUtil.getUTCTimeStr();

		Map<String, Object> model = new HashMap<>(16);

		model.put("code", randomCode);
		model.put("title", "找回密码");
		model.put("timeUTC", timeUTC);

		String subjcet = "[IPEX]找回密码";

		MessageResult result = this.sentCode(account, "registerCodeEmail.ftl", subjcet, model);

		if (result.getCode() == 0) {
			ValueOperations valueOperations = redisTemplate.opsForValue();
			String key = SysConstant.RESET_PASSWORD_CODE_PREFIX + account;
			valueOperations.getOperations().delete(key);
			// 缓存验证码
			valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);

			return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
		} else {
			return error(localeMessageSourceService.getMessage("SEND_FAILED"));
		}
	}

	private static final String SUBJECT_CHECK = "[IPEX]邮件验证码";

	/**
	 * 币币账户转账发送验证码
	 *
	 * @return
	 */
	@PostMapping("/exange/transfer/code")
	public MessageResult exangeTransferCode(@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());

		if (member == null || member.getEmail() == null) {
			return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
		}

		return sendEmailCodeMethod(SysConstant.EMAIL_EXANGE_TRANSFER_PREFIX, member.getEmail(), SUBJECT_CHECK);
	}

	/**
	 * 提币发送验证码
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withdraw/code")
	public MessageResult withdrawCode(@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());

		if (member == null || member.getEmail() == null) {
			return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
		}

		return sendEmailCodeMethod(SysConstant.EMAIL_WITHDRAW_CODE_PREFIX, member.getEmail(), SUBJECT_CHECK);
	}
	
	/**
	 * 提币地址管理发送验证码
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withdraw/address/code")
	public MessageResult withdrawAddressManageCode(@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());

		if (member == null || member.getEmail() == null) {
			return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
		}

		return sendEmailCodeMethod(SysConstant.EMAIL_WITHDRAW_ADDRESS_CODE_PREFIX, member.getEmail(), SUBJECT_CHECK);
	}

}
