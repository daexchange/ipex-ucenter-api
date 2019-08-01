package ai.turbochain.ipex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GS
 * @Description: coin
 * @date 2018/4/214:20
 */
@Slf4j
@RestController
@RequestMapping("coin")
public class CoinController extends BaseController {
    @Autowired
    private CoinService coinService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("legal")
    public MessageResult legal() {
        List<Coin> legalAll = coinService.findLegalAll();
        return success(legalAll);
    }

    @GetMapping("legal/page")
    public MessageResult findLegalCoinPage(PageModel pageModel) {
        Page all = coinService.findLegalCoinPage(pageModel);
        return success(all);
    }

    @RequestMapping("supported")
    public List<Map<String,String>>  findCoins(){
        List<Coin> coins = coinService.findAll();
        List<Map<String,String>> result = new ArrayList<>();
        coins.forEach(coin->{
            if(coin.getHasLegal().equals(Boolean.FALSE)) {
                Map<String, String> map = new HashMap<>();
                map.put("name",coin.getName());
                map.put("nameCn",coin.getNameCn());
                map.put("withdrawFee",String.valueOf(coin.getMinTxFee()));
                map.put("enableRecharge",String.valueOf(coin.getCanRecharge().getOrdinal()));
                map.put("minWithdrawAmount",String.valueOf(coin.getMinWithdrawAmount()));
                map.put("enableWithdraw",String.valueOf(coin.getCanWithdraw().getOrdinal()));
                result.add(map);
            }
        });
        return result;
    }
    
    
    @GetMapping("/cny-rate/{symbol}")
    public MessageResult CoinCnyRate(@PathVariable("symbol") String symbol) {
    	
    	String key = SysConstant.DIGITAL_CURRENCY_MARKET_PREFIX + symbol;

    	ValueOperations valueOperations = redisTemplate.opsForValue();
       
    	Object bondvalue =valueOperations.get(key);
          
        if (bondvalue==null) {
            log.info(">>>>>>缓存中无利率转换数据>>>>>"+symbol);
        } else {
            log.info(symbol+">>>>缓存中利率转换数据>>>"+bondvalue);
        }
          
        return success(bondvalue);
    }
}
