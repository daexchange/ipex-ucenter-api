package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.entity.TransferOtherRecord;
import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.WalletTransferOtherRecordService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/transfer-other/record")
@Slf4j
public class TransferOtherRecordController {
   
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
    @RequestMapping(value = "/page")
    public MessageResult page(@SessionAttribute(SESSION_MEMBER) AuthMember member, 
    		@RequestParam(value = "page", defaultValue = "1") Integer pageNo, 
    		@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
    	MessageResult result = MessageResult.success();
        
        Page<TransferOtherRecord> pageList = walletTransferOtherRecordService.queryRewardPromotionPage(pageNo, pageSize, memberService.findOne(member.getId()));
		 
        result.setData(pageList.getContent());
        result.setTotalPage(pageList.getTotalPages() + "");
        result.setTotalElement(pageList.getTotalElements() + "");
        
        return result;
    }
}
