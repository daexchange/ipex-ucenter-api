package ai.turbochain.ipex.controller;


import static ai.turbochain.ipex.constant.SysConstant.SESSION_MEMBER;

import java.text.ParseException;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ai.turbochain.ipex.entity.transform.AuthMember;
import ai.turbochain.ipex.service.TransferSelfRecordService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/transfer-self/record/")
@Slf4j
public class TransferSelfRecordController {
  
    @Autowired
    private TransferSelfRecordService transferSelfRecordService;

    /**
     * 查询所有记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/page")
    public MessageResult page(@SessionAttribute(SESSION_MEMBER) AuthMember member, 
    						HttpServletRequest request, int pageNo, int pageSize,
                            @RequestParam(value = "startTime",required = false) String startTime,
                            @RequestParam(value = "endTime",required = false) String endTime,
                            @RequestParam(value = "symbol",required = false) String symbol,
                            @RequestParam(value = "type",required = false) Integer type) throws ParseException {
        
    	MessageResult mr = new MessageResult();
      
        mr.setCode(0);
        mr.setMessage("success");
        mr.setData(transferSelfRecordService.queryByMember(member.getId(), pageNo, pageSize, type, startTime, endTime,symbol));
        
        return mr;
    }

}
