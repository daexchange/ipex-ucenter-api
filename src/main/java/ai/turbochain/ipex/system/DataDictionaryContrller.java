package ai.turbochain.ipex.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.entity.DataDictionary;
import ai.turbochain.ipex.service.DataDictionaryService;
import ai.turbochain.ipex.util.MessageResult;

/**
 * @author GS
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:21
 */
@RestController
@RequestMapping("data-dictionary")
public class DataDictionaryContrller extends BaseController {
    @Autowired
    private DataDictionaryService service;

    @GetMapping("{bond}")
    public MessageResult get(@PathVariable("bond") String bond) {
        DataDictionary data = service.findByBond(bond);
        if (data == null) {
            return error("validate bond");
        }
        return success((Object) data.getValue());
    }

}
