package ai.turbochain.ipex.config;

import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.service.CoinService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.turbochain.ipex.system.CoinExchangeFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 *	初始化数字货币列表
 * @author fly
 *
 */
@Configuration
public class CoinExchangeFactoryConfig {
    @Autowired
    private CoinService coinService;

    @Bean
    public CoinExchangeFactory createCoinExchangeFactory() {
        List<Coin> coinList = coinService.findAll();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coinList.forEach(coin ->
                factory.set(coin.getUnit(), new BigDecimal(coin.getUsdRate()), new BigDecimal(coin.getCnyRate()))
        );
        return factory;
    }
}
