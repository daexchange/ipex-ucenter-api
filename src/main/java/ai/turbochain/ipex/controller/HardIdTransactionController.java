package ai.turbochain.ipex.controller;

import static ai.turbochain.ipex.constant.BooleanEnum.IS_FALSE;
import static ai.turbochain.ipex.constant.BooleanEnum.IS_TRUE;
import static ai.turbochain.ipex.constant.CertifiedBusinessStatus.CANCEL_AUTH;
import static ai.turbochain.ipex.constant.CertifiedBusinessStatus.RETURN_FAILED;
import static ai.turbochain.ipex.constant.CertifiedBusinessStatus.RETURN_SUCCESS;
import static ai.turbochain.ipex.constant.CertifiedBusinessStatus.VERIFIED;
import static ai.turbochain.ipex.constant.SysConstant.API_HARD_ID_MEMBER;
import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;
import static ai.turbochain.ipex.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;

import ai.turbochain.ipex.constant.AuditStatus;
import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.CertifiedBusinessStatus;
import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.constant.MemberLevelEnum;
import ai.turbochain.ipex.constant.RealNameStatus;
import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.constant.WithdrawStatus;
import ai.turbochain.ipex.entity.Alipay;
import ai.turbochain.ipex.entity.BankInfo;
import ai.turbochain.ipex.entity.BindAli;
import ai.turbochain.ipex.entity.BindBank;
import ai.turbochain.ipex.entity.BindWechat;
import ai.turbochain.ipex.entity.BusinessAuthApply;
import ai.turbochain.ipex.entity.BusinessAuthDeposit;
import ai.turbochain.ipex.entity.BusinessCancelApply;
import ai.turbochain.ipex.entity.CertifiedBusinessInfo;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Country;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberAccount;
import ai.turbochain.ipex.entity.MemberApplication;
import ai.turbochain.ipex.entity.MemberDeposit;
import ai.turbochain.ipex.entity.MemberLegalCurrencyWallet;
import ai.turbochain.ipex.entity.OtcCoin;
import ai.turbochain.ipex.entity.QMemberApplication;
import ai.turbochain.ipex.entity.ScanWithdrawRecord;
import ai.turbochain.ipex.entity.WechatPay;
import ai.turbochain.ipex.entity.WithdrawRecord;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.exception.InformationExpiredException;
import ai.turbochain.ipex.pagination.PageResult;
import ai.turbochain.ipex.service.BusinessAuthApplyService;
import ai.turbochain.ipex.service.BusinessAuthDepositService;
import ai.turbochain.ipex.service.BusinessCancelApplyService;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.CountryService;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberAddressService;
import ai.turbochain.ipex.service.MemberApplicationService;
import ai.turbochain.ipex.service.MemberDepositService;
import ai.turbochain.ipex.service.MemberLegalCurrencyWalletService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.service.OtcCoinService;
import ai.turbochain.ipex.service.WithdrawRecordService;
import ai.turbochain.ipex.util.BindingResultUtil;
import ai.turbochain.ipex.util.Md5;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * hard-id资金交易密码管理
 *
 * @author andrew
 * @date 2019年12月11日
 */
@RestController
@RequestMapping("/hard-id/transaction")
@Slf4j
public class HardIdTransactionController {

	@Autowired
	private MemberService memberService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private LocaleMessageSourceService msService;
	@Autowired
	private MemberApplicationService memberApplicationService;
	@Autowired
	private BusinessAuthDepositService businessAuthDepositService;
	@Autowired
	private BusinessCancelApplyService businessCancelApplyService;
	@Autowired
	private MemberLegalCurrencyWalletService memberLegalCurrencyWalletService;
	@Autowired
	private BusinessAuthApplyService businessAuthApplyService;
	@Autowired
	private MemberDepositService memberDepositService;
	@Autowired
	private CountryService countryService;
	@Autowired
	private OtcCoinService otcCoinService;
	@Autowired
	private CoinService coinService;
	@Autowired
	private MemberWalletService memberWalletService;
	@Autowired
	private WithdrawRecordService withdrawApplyService;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private MemberAddressService memberAddressService;

	/**
	 * 设置资金密码
	 *
	 * @param jyPassword
	 * @param user
	 * @return
	 */
	@RequestMapping("/password")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult approveTransaction(String jyPassword, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user)
			throws Exception {

		hasText(jyPassword, msService.getMessage("MISSING_JY_PASSWORD"));
		isTrue(jyPassword.length() >= 6 && jyPassword.length() <= 20,
				msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));

		Member member = memberService.findOne(user.getId());
		Assert.isNull(member.getJyPassword(), msService.getMessage("REPEAT_SETTING"));
		// 生成密码
		String jyPass = Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase();
		member.setJyPassword(jyPass);
		return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
	}

	/**
	 * 修改资金密码
	 *
	 * @param oldPassword
	 * @param newPassword
	 * @param user
	 * @return
	 */
	@RequestMapping("/update/password")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateTransaction(String oldPassword, String newPassword,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		hasText(oldPassword, msService.getMessage("MISSING_OLD_JY_PASSWORD"));
		hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
		isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
				msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
		Member member = memberService.findOne(user.getId());
		isTrue(Md5.md5Digest(oldPassword + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
				msService.getMessage("ERROR_JYPASSWORD"));
		member.setJyPassword(Md5.md5Digest(newPassword + member.getSalt()).toLowerCase());
		return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
	}

	/**
	 * 发起HardId OTC 提现
	 * 
	 * @param password
	 * @param unit
	 * @param address
	 * @param amount
	 * @param fee
	 * @param remark
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withdraw")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult withdraw(String jyPassword, String unit, String address, BigDecimal amount, BigDecimal fee,
			String remark, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		hasText(jyPassword, msService.getMessage("MISSING_PASSWORD"));
		hasText(unit, msService.getMessage("MISSING_COIN_TYPE"));
		Member member = memberService.findOne(user.getId());
		isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
				msService.getMessage("ERROR_JYPASSWORD"));
		OtcCoin otcCoin = otcCoinService.findByUnit(unit);
		notNull(otcCoin, msService.getMessage("OTCCOIN_ILLEGAL"));
		Coin coin = coinService.findByUnit(unit);
		notNull(coin, msService.getMessage("COIN_ILLEGAL"));
		MemberLegalCurrencyWallet memberLegalCurrencyWallet = memberLegalCurrencyWalletService
				.findByOtcCoinUnitAndMemberId(unit, user.getId());
		MessageResult result = memberLegalCurrencyWalletService.freezeBalance(memberLegalCurrencyWallet, amount);
		if (result.getCode() <= 0) {
			throw new InformationExpiredException("Information Expired");
		}
		WithdrawRecord withdrawApply = new WithdrawRecord();
		withdrawApply.setCoin(coin);
		withdrawApply.setFee(fee);
		withdrawApply.setArrivedAmount(sub(amount, fee));
		withdrawApply.setMemberId(user.getId());
		withdrawApply.setTotalAmount(amount);
		withdrawApply.setAddress(address);
		withdrawApply.setRemark(remark);
		withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
		withdrawApply.setStatus(WithdrawStatus.WAITING);
		withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
		withdrawApply.setDealTime(withdrawApply.getCreateTime());
		WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
		JSONObject json = new JSONObject();
		json.put("uid", user.getId());
		// 提币总数量
		json.put("totalAmount", amount);
		// 手续费
		json.put("fee", fee);
		// 预计到账数量
		json.put("arriveAmount", sub(amount, fee));
		json.put("coin", coin);
		json.put("address", address);
		json.put("withdrawId", withdrawRecord.getId());
		kafkaTemplate.send("hardId-withdraw", coin.getUnit(), json.toJSONString());
		result = MessageResult.success(msService.getMessage("START_HARDID_WITHDRAW"));
		result.setData(withdrawRecord.getId());
		return result;
	}

	/**
	 * 提币状态（ 0:审核中，1：等待放币，2：失败，3：成功）
	 * 
	 * @param withdrawRecordId
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withdraw/status")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult withdrawStatus(Long withdrawRecordId, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user)
			throws Exception {
		hasText(withdrawRecordId.toString(), msService.getMessage("MISSING_WITHDRAW_RECORDID"));
		WithdrawRecord withdrawRecord = withdrawApplyService.findOne(withdrawRecordId);
		notNull(withdrawRecord, msService.getMessage("WITHDRAW_RECORD_ILLEGAL"));
		MessageResult result = MessageResult.success(msService.getMessage("HARDID_WITHDRAW_STATUS"));
		result.setData(withdrawRecord.getStatus());
		return result;
	}

	/**
	 * 提币记录
	 * 
	 * @param user
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/withdraw/record")
	public MessageResult pageWithdraw(int page, int pageSize, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) {
		MessageResult mr = new MessageResult(0, "success");
		Page<WithdrawRecord> records = withdrawApplyService.findAllByMemberId(user.getId(), page - 1, pageSize);
		records.map(x -> ScanWithdrawRecord.toScanWithdrawRecord(x));
		mr.setData(records);
		return mr;
	}

	/**
	 * 提币记录
	 * 
	 * @param user
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/deposit/record")
	public MessageResult pageDeposit(int page, int pageSize, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) {
		MessageResult mr = new MessageResult(0, "success");
		Page<MemberDeposit> records = memberDepositService.findAllByMemberId(user.getId(), page - 1, pageSize);
		mr.setData(records);
		return mr;
	}

	/**
	 * 设置提现地址
	 * 
	 * @param address
	 * @param remark
	 * @param coinUnit
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/update/withdraw-address")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateWithdrawAddrss(String address, String remark, String coinName,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		hasText(address, msService.getMessage("MISSING_ADDRESS"));
		hasText(coinName, msService.getMessage("MISSING_COIN_NAME"));
		OtcCoin otcCoin = otcCoinService.findByName(coinName);
		notNull(otcCoin, msService.getMessage("OTCCOIN_ILLEGAL"));
		MessageResult result = memberAddressService.addOrUpdateMemberAddress(user.getId(), address, coinName, remark);
		return result;
	}

	/**
	 * 查询提现地址
	 * 
	 * @param memberAddressId
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/select/withdraw-address")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult selectWithdrawAddrss(String coinName, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user)
			throws Exception {
		hasText(coinName, msService.getMessage("MISSING_COIN_NAME"));
		MessageResult result = memberAddressService.findMemberAddressByCoinName(user.getId(), coinName);
		return result;
	}

	/**
	 * 实名认证
	 *
	 * @param realName
	 * @param idCard
	 * @param user
	 * @return
	 */
	@RequestMapping("/real-name/authentication")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult realApprove(String country, String realName, String idCard, String idCardFront,
			String idCardBack, String handHeldIdCard, @SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) {
		hasText(realName, msService.getMessage("MISSING_REAL_NAME"));

		hasText(idCard, msService.getMessage("MISSING_ID_CARD"));
		hasText(idCardFront, msService.getMessage("MISSING_ID_CARD_FRONT"));
		hasText(idCardBack, msService.getMessage("MISSING_ID_CARD_BACK"));
		hasText(handHeldIdCard, msService.getMessage("MISSING_ID_CARD_HAND"));

		Member member = memberService.findOne(user.getId());

		// 修改所在国家
		if (org.apache.commons.lang3.StringUtils.isNotBlank(country)) {
			Country one = countryService.findOne(country);
			if (one != null) {
				member.setCountry(one);
				memberService.saveAndFlush(member);
			}
		}
		// if ("China".equals(member.getCountry().getEnName())) {
		// isTrue(ValidateUtil.isChineseName(realName),
		// msService.getMessage("REAL_NAME_ILLEGAL"));
		// isTrue(IdcardValidator.isValidate18Idcard(idCard),
		// msService.getMessage("ID_CARD_ILLEGAL"));
		// }
		isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED,
				msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
		int count = memberApplicationService.queryByIdCard(idCard);
		if (count > 0) {
			// TODO 国际化为护照号码
			return MessageResult.error("同一个护照号码只能认证一次");
		}
		MemberApplication memberApplication = new MemberApplication();
		memberApplication.setAuditStatus(AuditStatus.AUDIT_ING);
		memberApplication.setRealName(realName);
		memberApplication.setIdCard(idCard);
		memberApplication.setMember(member);
		memberApplication.setIdentityCardImgFront(idCardFront);// 身份证正面照
		memberApplication.setIdentityCardImgInHand(handHeldIdCard);// 手持照片
		memberApplication.setIdentityCardImgReverse(idCardBack);
		memberApplication.setCreateTime(new Date());
		// TODO
		memberApplication.setCardType(1);// 身份证

		memberApplicationService.save(memberApplication);
		member.setRealNameStatus(RealNameStatus.AUDITING);
		return MessageResult.success(msService.getMessage("REAL_APPLY_SUCCESS"));
	}

	/**
	 * 设置银行卡
	 *
	 * @param bindBank
	 * @param bindingResult
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bind/bank")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult bindBank(@Valid BindBank bindBank, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());
		isTrue(member.getBankInfo() == null, msService.getMessage("REPEAT_SETTING"));
		return doBank(bindBank, bindingResult, user);
	}

	private MessageResult doBank(BindBank bindBank, BindingResult bindingResult, AuthMember user) throws Exception {
		MessageResult result = BindingResultUtil.validate(bindingResult);
		if (result != null) {
			return result;
		}
		Member member = memberService.findOne(user.getId());
		isTrue(Md5.md5Digest(bindBank.getJyPassword() + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
				msService.getMessage("ERROR_JYPASSWORD"));
		BankInfo bankInfo = new BankInfo();
		bankInfo.setBank(bindBank.getBank());
		bankInfo.setBranch(bindBank.getBranch());
		bankInfo.setCardNo(bindBank.getCardNo());
		member.setBankInfo(bankInfo);
		return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
	}

	/**
	 * 更改银行卡
	 *
	 * @param bindBank
	 * @param bindingResult
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/update/bank")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateBank(@Valid BindBank bindBank, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		return doBank(bindBank, bindingResult, user);
	}

	/**
	 * 绑定阿里
	 *
	 * @param bindAli
	 * @param bindingResult
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bind/ali")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult bindAli(@Valid BindAli bindAli, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());
		isTrue(member.getAlipay() == null, msService.getMessage("REPEAT_SETTING"));
		return doAli(bindAli, bindingResult, user);
	}

	private MessageResult doAli(BindAli bindAli, BindingResult bindingResult, AuthMember user) throws Exception {
		MessageResult result = BindingResultUtil.validate(bindingResult);
		if (result != null) {
			return result;
		}
		Member member = memberService.findOne(user.getId());
		isTrue(Md5.md5Digest(bindAli.getJyPassword() + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
				msService.getMessage("ERROR_JYPASSWORD"));
		Alipay alipay = new Alipay();
		alipay.setAliNo(bindAli.getAli());
		alipay.setQrCodeUrl(bindAli.getQrCodeUrl());
		member.setAlipay(alipay);
		return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
	}

	/**
	 * 修改支付宝
	 *
	 * @param bindAli
	 * @param bindingResult
	 * @param user
	 * @return
	 */
	@RequestMapping("/update/ali")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateAli(@Valid BindAli bindAli, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		return doAli(bindAli, bindingResult, user);
	}

	/**
	 * 绑定微信
	 *
	 * @param bindWechat
	 * @param bindingResult
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bind/wechat")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult bindWechat(@Valid BindWechat bindWechat, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());
		isTrue(member.getWechatPay() == null, msService.getMessage("REPEAT_SETTING"));
		return doWechat(bindWechat, bindingResult, user);
	}

	private MessageResult doWechat(BindWechat bindWechat, BindingResult bindingResult, AuthMember user)
			throws Exception {
		MessageResult result = BindingResultUtil.validate(bindingResult);
		if (result != null) {
			return result;
		}
		Member member = memberService.findOne(user.getId());
		isTrue(Md5.md5Digest(bindWechat.getJyPassword() + member.getSalt()).toLowerCase()
				.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
		WechatPay wechatPay = new WechatPay();
		wechatPay.setWechat(bindWechat.getWechat());
		wechatPay.setQrWeCodeUrl(bindWechat.getQrCodeUrl());
		member.setWechatPay(wechatPay);
		return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
	}

	/**
	 * 修改微信
	 *
	 * @param bindWechat
	 * @param bindingResult
	 * @param user
	 * @return
	 */
	@RequestMapping("/update/wechat")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateWechat(@Valid BindWechat bindWechat, BindingResult bindingResult,
			@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) throws Exception {
		return doWechat(bindWechat, bindingResult, user);
	}

	/**
	 * 查询实名认证情况
	 *
	 * @param user
	 * @return
	 */
	@PostMapping("/real/detail")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult realNameApproveDetail(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		List<Predicate> predicateList = new ArrayList<>();
		predicateList.add(QMemberApplication.memberApplication.member.eq(member));
		PageResult<MemberApplication> memberApplicationPageResult = memberApplicationService.query(predicateList, null,
				null);
		MemberApplication memberApplication = new MemberApplication();
		if (memberApplicationPageResult != null && memberApplicationPageResult.getContent() != null
				&& memberApplicationPageResult.getContent().size() > 0) {
			memberApplication = memberApplicationPageResult.getContent().get(0);
		}
		MessageResult result = MessageResult.success();
		result.setData(memberApplication);
		return result;
	}

	/**
	 * 重置资金密码
	 *
	 * @param newPassword
	 * @param code
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/reset/password")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult resetTransaction(String newPassword, String code,
			@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
		isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
				msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
		ValueOperations valueOperations = redisTemplate.opsForValue();
		// 邮箱验证码
		Object cache = valueOperations.get(SysConstant.EMAIL_RESET_TRANS_CODE_PREFIX + user.getEmail());
		// 前台忘记资金密码不再使用手机验证码
//        Object cache = valueOperations.get(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + user.getMobilePhone());
		notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
		hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
		if (!code.equals(cache.toString())) {
			return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
		} else {
//            valueOperations.getOperations().delete(SysConstant.PHONE_RESET_TRANS_CODE_PREFIX + user.getMobilePhone());
			valueOperations.getOperations().delete(SysConstant.EMAIL_RESET_TRANS_CODE_PREFIX + user.getEmail());
		}
		Member member = memberService.findOne(user.getId());
		member.setJyPassword(Md5.md5Digest(newPassword + member.getSalt()).toLowerCase());
		return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
	}

	/**
	 * 绑定手机号
	 *
	 * @param password
	 * @param phone
	 * @param code
	 * @param user
	 * @return
	 */
	@RequestMapping("/bind/phone")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult bindPhone(HttpServletRequest request, String password, String phone, String code,
			@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
		hasText(phone, msService.getMessage("MISSING_PHONE"));
		hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
		if ("中国".equals(user.getLocation().getCountry())) {
			if (!ValidateUtil.isMobilePhone(phone.trim())) {
				return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
			}
		}
		ValueOperations valueOperations = redisTemplate.opsForValue();
		Object cache = valueOperations.get(SysConstant.PHONE_BIND_CODE_PREFIX + phone);

//        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));

		Member member1 = memberService.findByPhone(phone);
		isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
//        临时注释
//        if (!code.equals(cache.toString())) {
//            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
//        } else {
//            valueOperations.getOperations().delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
//        }

		Member member = memberService.findOne(user.getId());
		isTrue(member.getMobilePhone() == null, msService.getMessage("REPEAT_PHONE_REQUEST"));
		if (member.getPassword().equals(Md5.md5Digest(password + member.getSalt()).toLowerCase())) {
			member.setMobilePhone(phone);
			return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
		} else {
			request.removeAttribute(SysConstant.SESSION_MEMBER);
			return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
		}
	}

	/**
	 *
	 * 更改登录密码
	 *
	 * @param request
	 * @param oldPassword
	 * @param newPassword
	 * @param code
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/update/password1")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateLoginPassword(HttpServletRequest request, String oldPassword, String newPassword,
			String code, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		hasText(oldPassword, msService.getMessage("MISSING_OLD_PASSWORD"));
		hasText(newPassword, msService.getMessage("MISSING_NEW_PASSWORD"));
		isTrue(newPassword.length() >= 6 && newPassword.length() <= 20,
				msService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
		ValueOperations valueOperations = redisTemplate.opsForValue();
		// 手机验证码缓存
//        Object cache = valueOperations.get(SysConstant.PHONE_UPDATE_PASSWORD_PREFIX + user.getMobilePhone());
		// 邮箱验证码缓存
		Object cache = valueOperations.get(SysConstant.EMAIL_UPDATE_PASSWORD_PREFIX + user.getEmail());
		notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
		hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
		if (!code.equals(cache.toString())) {
			return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
		} else {
			valueOperations.getOperations().delete(SysConstant.EMAIL_UPDATE_PASSWORD_PREFIX + user.getEmail());
		}
		Member member = memberService.findOne(user.getId());
		request.removeAttribute(SysConstant.SESSION_MEMBER);
		isTrue(Md5.md5Digest(oldPassword + member.getSalt()).toLowerCase().equals(member.getPassword()),
				msService.getMessage("PASSWORD_ERROR"));
		member.setPassword(Md5.md5Digest(newPassword + member.getSalt()).toLowerCase());
		return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
	}

	/**
	 * 绑定邮箱
	 *
	 * @param request
	 * @param password
	 * @param code
	 * @param email
	 * @param user
	 * @return
	 */
	@RequestMapping("/bind/email")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult bindEmail(HttpServletRequest request, String password, String code, String email,
			@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
		hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
		hasText(email, msService.getMessage("MISSING_EMAIL"));
		isTrue(ValidateUtil.isEmail(email), msService.getMessage("EMAIL_FORMAT_ERROR"));
		ValueOperations valueOperations = redisTemplate.opsForValue();
		Object cache = valueOperations.get(SysConstant.EMAIL_BIND_CODE_PREFIX + email);
		notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
		isTrue(code.equals(cache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
		Member member = memberService.findOne(user.getId());
		isTrue(member.getEmail() == null, msService.getMessage("REPEAT_EMAIL_REQUEST"));
		if (!Md5.md5Digest(password + member.getSalt()).toLowerCase().equals(member.getPassword())) {
			request.removeAttribute(SysConstant.SESSION_MEMBER);
			return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
		} else {
			member.setEmail(email);
			return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
		}
	}

	/**
	 * 账户设置
	 *
	 * @param user
	 * @return
	 */
	@RequestMapping("/account/setting")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult accountSetting(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		hasText(member.getIdNumber(), msService.getMessage("NO_REAL_NAME"));
		hasText(member.getJyPassword(), msService.getMessage("NO_JY_PASSWORD"));
		MemberAccount memberAccount = MemberAccount.builder().alipay(member.getAlipay())
				.aliVerified(member.getAlipay() == null ? IS_FALSE : IS_TRUE).bankInfo(member.getBankInfo())
				.bankVerified(member.getBankInfo() == null ? IS_FALSE : IS_TRUE).wechatPay(member.getWechatPay())
				.wechatVerified(member.getWechatPay() == null ? IS_FALSE : IS_TRUE).realName(member.getRealName())
				.build();
		MessageResult result = MessageResult.success();
		result.setData(memberAccount);
		return result;
	}

	/**
	 * 认证商家申请状态
	 *
	 * @param user
	 * @return
	 */
	@RequestMapping("/certified/business/status")
	public MessageResult certifiedBusinessStatus(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
		certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());
		certifiedBusinessInfo.setEmail(member.getEmail());
		certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
		// logger.info("会员状态信息:{}",certifiedBusinessInfo);
		if (member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED)) {
			List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService
					.findByMemberAndCertifiedBusinessStatus(member, member.getCertifiedBusinessStatus());
			// logger.info("会员申请商家认证信息:{}",businessAuthApplyList);
			if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
				certifiedBusinessInfo
						.setCertifiedBusinessStatus(businessAuthApplyList.get(0).getCertifiedBusinessStatus());
				// logger.info("会员申请商家认证最新信息:{}",businessAuthApplyList.get(0));
				certifiedBusinessInfo.setDetail(businessAuthApplyList.get(0).getDetail());
			}
		}

		List<BusinessCancelApply> businessCancelApplies = businessCancelApplyService.findByMember(member);
		if (businessCancelApplies != null && businessCancelApplies.size() > 0) {
			if (businessCancelApplies.get(0).getStatus() == RETURN_SUCCESS) {
				if (member.getCertifiedBusinessStatus() != VERIFIED) {
					certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_SUCCESS);
				}
			} else if (businessCancelApplies.get(0).getStatus() == RETURN_FAILED) {
				certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_FAILED);
			} else {
				certifiedBusinessInfo.setCertifiedBusinessStatus(CANCEL_AUTH);
			}
		}

		MessageResult result = MessageResult.success();
		result.setData(certifiedBusinessInfo);
		return result;
	}

	/**
	 * 认证商家申请
	 *
	 * @param user
	 * @return
	 */
	@RequestMapping("/certified/business/apply")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult certifiedBusiness(@SessionAttribute(SESSION_MEMBER) AuthMember user, String json,
			@RequestParam Long businessAuthDepositId) {
		Member member = memberService.findOne(user.getId());
		// 只有未认证和认证失败的用户，可以发起认证申请
		isTrue(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.NOT_CERTIFIED)
				|| member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED),
				msService.getMessage("REPEAT_APPLICATION"));
		isTrue(member.getMemberLevel().equals(MemberLevelEnum.REALNAME), msService.getMessage("NO_REAL_NAME"));
		// hasText(member.getEmail(), msService.getMessage("NOT_BIND_EMAIL"));
		List<BusinessAuthDeposit> depositList = businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
		// 如果当前有启用的保证金类型，必须选择一种保证金才可以申请商家认证
		BusinessAuthDeposit businessAuthDeposit = null;
		if (depositList != null && depositList.size() > 0) {
			if (businessAuthDepositId == null) {
				return MessageResult.error("must select a kind of business auth deposit");
			}
			boolean flag = false;
			for (BusinessAuthDeposit deposit : depositList) {
				if (deposit.getId().equals(businessAuthDepositId)) {
					businessAuthDeposit = deposit;
					flag = true;
				}
			}
			if (!flag) {
				return MessageResult.error("business auth deposit is not found");
			}
			MemberLegalCurrencyWallet memberLegalCurrencyWallet = null;// memberLegalCurrencyWalletService.findByCoinUnitAndMemberId(businessAuthDeposit.getCoin().getUnit(),member.getId());
			if (memberLegalCurrencyWallet.getBalance().compareTo(businessAuthDeposit.getAmount()) < 0) {
				return MessageResult.error("您的余额不足");
			}
			// 冻结保证金需要的金额
			memberLegalCurrencyWallet
					.setBalance(memberLegalCurrencyWallet.getBalance().subtract(businessAuthDeposit.getAmount()));
			memberLegalCurrencyWallet.setFrozenBalance(
					memberLegalCurrencyWallet.getFrozenBalance().add(businessAuthDeposit.getAmount()));
		}
		// 申请记录
		BusinessAuthApply businessAuthApply = new BusinessAuthApply();
		businessAuthApply.setCreateTime(new Date());
		businessAuthApply.setAuthInfo(json);
		businessAuthApply.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
		businessAuthApply.setMember(member);
		// 不一定会有保证金策略
		if (businessAuthDeposit != null) {
			businessAuthApply.setBusinessAuthDeposit(businessAuthDeposit);
			businessAuthApply.setAmount(businessAuthDeposit.getAmount());
		}
		businessAuthApplyService.create(businessAuthApply);

		member.setCertifiedBusinessApplyTime(new Date());
		member.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
		CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
		certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());
		certifiedBusinessInfo.setEmail(member.getEmail());
		certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
		MessageResult result = MessageResult.success();
		result.setData(certifiedBusinessInfo);
		return result;
	}

	@RequestMapping("/business-auth-deposit/list")
	public MessageResult listBusinessAuthDepositList() {
		List<BusinessAuthDeposit> depositList = businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
		depositList.forEach(deposit -> {
			deposit.setAdmin(null);
		});
		MessageResult result = MessageResult.success();
		result.setData(depositList);
		return result;
	}

	/**
	 * 原来的手机还能用的情况下更换手机
	 *
	 * @param request
	 * @param password
	 * @param phone
	 * @param code
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/change/phone")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult changePhone(HttpServletRequest request, String password, String phone, String code,
			@SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
		Member member = memberService.findOne(user.getId());
		hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
		hasText(phone, msService.getMessage("MISSING_PHONE"));
		hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
		Member member1 = memberService.findByPhone(phone);
		isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
		ValueOperations valueOperations = redisTemplate.opsForValue();
		Object cache = valueOperations.get(SysConstant.PHONE_CHANGE_CODE_PREFIX + member.getMobilePhone());
		notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
		if ("86".equals(member.getCountry().getAreaCode())) {
			if (!ValidateUtil.isMobilePhone(phone.trim())) {
				return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
			}
		}
		if (member.getPassword().equals(Md5.md5Digest(password + member.getSalt()).toLowerCase())) {
			if (!code.equals(cache.toString())) {
				return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
			} else {
				valueOperations.getOperations().delete(SysConstant.PHONE_CHANGE_CODE_PREFIX + member.getMobilePhone());
			}
			member.setMobilePhone(phone);
			return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
		} else {
			request.removeAttribute(SysConstant.SESSION_MEMBER);
			return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
		}
	}

	/**
	 * 申请取消认证商家
	 * 
	 * @return
	 */
	@PostMapping("/cancel/business")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult cancelBusiness(@SessionAttribute(SESSION_MEMBER) AuthMember user,
			@RequestParam(value = "detail", defaultValue = "") String detail) {
		log.info("user:{}", user);
		Member member = memberService.findOne(user.getId());
		if (member.getCertifiedBusinessStatus() == CANCEL_AUTH) {
			return MessageResult.error("退保审核中，请勿重复提交......");
		}
		if (!member.getCertifiedBusinessStatus()
				.equals(CertifiedBusinessStatus.VERIFIED)/*
															 * && !member.getCertifiedBusinessStatus().equals(
															 * CertifiedBusinessStatus.RETURN_FAILED)
															 */) {
			return MessageResult.error("you are not verified business");
		}

		List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService
				.findByMemberAndCertifiedBusinessStatus(member, CertifiedBusinessStatus.VERIFIED);
		if (businessAuthApplyList == null || businessAuthApplyList.size() < 1) {
			return MessageResult.error("you are not verified business,business auth apply not exist......");
		}

		if (businessAuthApplyList.get(0).getCertifiedBusinessStatus() != CertifiedBusinessStatus.VERIFIED) {
			return MessageResult.error(
					"data exception, state inconsistency(CertifiedBusinessStatus in BusinessAuthApply and Member)");
		}

		member.setCertifiedBusinessStatus(CANCEL_AUTH);
		log.info("会员状态:{}", member.getCertifiedBusinessStatus());
		memberService.save(member);
		log.info("会员状态:{}", member.getCertifiedBusinessStatus());

		BusinessCancelApply cancelApply = new BusinessCancelApply();
		cancelApply.setDepositRecordId(businessAuthApplyList.get(0).getDepositRecordId());
		cancelApply.setMember(businessAuthApplyList.get(0).getMember());
		cancelApply.setStatus(CANCEL_AUTH);
		cancelApply.setReason(detail);
		log.info("退保申请状态:{}", cancelApply.getStatus());
		businessCancelApplyService.save(cancelApply);
		log.info("退保申请状态:{}", cancelApply.getStatus());

		return MessageResult.success();
	}

	/**
	 * 设置或更改昵称
	 *
	 * @param userName
	 * @return
	 */
	@RequestMapping("/update/userName")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult updateUserName(@SessionAttribute(SESSION_MEMBER) AuthMember user,
			@RequestParam(value = "userName", required = true) String userName) {

		Member member = memberService.findOne(user.getId());

		member.setUsername(userName);
		;

		return MessageResult.success();
	}

	/**
	 * 查询实名认证情况
	 *
	 * @param user
	 * @return
	 */
	@PostMapping("/user-infor")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult userInfor(@SessionAttribute(API_HARD_ID_MEMBER) AuthMember user) {
		Member member = memberService.findOne(user.getId());
		if (member == null) {
			return MessageResult.error("会员不存在");
		}
		Map<String, Object> data = new HashMap<String, Object>();

		data.put("member", member);

		if (StringUtils.isBlank(member.getJyPassword())) {
			data.put("isSetJyPassWord", 0);
		} else {
			data.put("isSetJyPassWord", 1);
		}

		MessageResult result = MessageResult.success();
		result.setData(data);
		return result;
	}
}
