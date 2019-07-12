package ai.turbochain.ipex.controller;

import com.netflix.discovery.converters.Auto;

import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.dto.SmsDTO;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.service.SmsService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Description:
 * @author: GuoShuai
 * @date: create in 10:47 2018/6/28
 * @Modified:
 */
@RestController
@RequestMapping("/smstest")
@ToString
public class TestController extends BaseController{
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private MemberWalletService memberWalletService;
    @RequestMapping(value = "sms",method = RequestMethod.GET)
    public MessageResult testSms(){
        SmsDTO smsDTO = smsService.getByStatus();
        System.out.println(smsDTO);
        System.out.println(success("succss",smsDTO));
        
        return success(smsDTO);
    }

    @RequestMapping(value = "drop",method = RequestMethod.GET)
    public MessageResult testDrop(){

        int drop = memberWalletService.dropWeekTable(1);
        System.out.println(drop+"..................");
//        int create = memberWalletService.createWeekTable(1);
//        System.out.println(create+"............");
        
        return null;
    }

}
