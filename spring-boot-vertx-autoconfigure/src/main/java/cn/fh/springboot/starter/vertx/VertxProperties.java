package cn.fh.springboot.starter.vertx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "vertx")
@Data
public class VertxProperties {
    /**
     * NIO线程数, 默认为CPU逻辑核心数量
     */
    private int nioThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * worker线程池大小
     */
    private int workerThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * 部署的http verticle数量
     */
    private int verticleCount = Runtime.getRuntime().availableProcessors();
    /**
     * NIO线程处理一个事件允许消耗的最长时间, 毫秒
     */
    private long maxEventLoopExecuteTime = 2000;

    /**
     * 请求路由配置
     */
    private List<HandlerMapping> handlerMappings;

    @Data
    public static class HandlerMapping {
        /**
         * 请求路径
         */
        private String path;
        /**
         * 对应Spring里的bean名
         */
        private List<String> beanNames;
    }
}
