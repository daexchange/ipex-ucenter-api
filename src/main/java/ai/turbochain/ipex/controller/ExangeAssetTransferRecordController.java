package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.entity.PromotionRewardRecord;
import ai.turbochain.ipex.entity.RewardRecord;
import ai.turbochain.ipex.entity.WalletTransferOtherRecord;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.WalletTransferOtherRecordService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/exange/transfer")
@Slf4j
public class ExangeAssetTransferRecordController {
   
	@Autowired
    private WalletTransferOtherRecordService walletTransferOtherRecordService;
	@Autowired
    private MemberService memberService;
	
	/**
     * 转账记录
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "/record")
    public MessageResult record(@SessionAttribute(SESSION_MEMBER) AuthMember member, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        MessageResult result = MessageResult.success();
        
        Page<WalletTransferOtherRecord> pageList = walletTransferOtherRecordService.queryRewardPromotionPage(pageNo, pageSize, memberService.findOne(member.getId()));
		 
        result.setData(pageList.getContent());
        result.setTotalPage(pageList.getTotalPages() + "");
        result.setTotalElement(pageList.getTotalElements() + "");
        
        return result;
    }
}
