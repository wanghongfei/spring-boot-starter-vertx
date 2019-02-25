package cn.fh.springboot.starter.vertx;

import cn.fh.springboot.starter.vertx.verticle.VerticleDeployBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Vertx Http Server自动配置类
 * Created by wanghongfei on 2019-02-25.
 */
@Configuration
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public VerticleDeployBean verticleDeployBean() {
        return new VerticleDeployBean();
    }
}
