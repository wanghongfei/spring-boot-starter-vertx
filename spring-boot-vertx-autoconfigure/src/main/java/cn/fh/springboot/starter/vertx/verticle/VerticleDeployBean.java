package cn.fh.springboot.starter.vertx.verticle;

import cn.fh.springboot.starter.vertx.VertxProperties;
import cn.fh.springboot.starter.vertx.error.VertxDeploymentException;
import com.alibaba.fastjson.JSON;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanghongfei on 2019-02-25.
 */
@Slf4j
public class VerticleDeployBean implements ApplicationContextAware {
    private volatile Throwable bootError;

    private ApplicationContext springContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }

    /**
     * 部署verticle, 启动Server
     */
    @PostConstruct
    public void startHttpServer() {
        VertxProperties props = springContext.getBean(VertxProperties.class);
        log.info("vertx config: {}", JSON.toJSONString(props));

        List<VertxProperties.HandlerMapping> mappings = props.getHandlerMappings();
        if (CollectionUtils.isEmpty(mappings)) {
            throw new IllegalStateException("no handler mappings found");
        }

        logMappings(springContext, mappings);

        deploy(springContext, props);
    }

    /**
     * 此方法只是打印一下路由信息
     */
    private void logMappings(ApplicationContext ctx, List<VertxProperties.HandlerMapping> mappings) {
        for (VertxProperties.HandlerMapping hm : mappings) {
            List<String> classNames = new ArrayList<>(hm.getBeanNames().size());
            for (String beanName : hm.getBeanNames()) {
                classNames.add(
                        ctx.getBean(beanName).getClass().getName()
                );
            }

            log.info("mapping {} to {}", hm.getPath(), classNames);
        }

    }

    /**
     * 部署http verticle
     *
     * @param context
     * @param config
     */
    private void deploy(ApplicationContext context, VertxProperties config) {
        // 创建vertx
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(config.getNioThreadCount())
                .setWorkerPoolSize(config.getWorkerThreadCount())
                .setMaxEventLoopExecuteTime(config.getMaxEventLoopExecuteTime())
                .setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);


        Vertx vertx = Vertx.vertx(vertxOptions);

        DeploymentOptions depOptions = new DeploymentOptions();
        depOptions.setInstances(config.getVerticleCount());

        HttpServerVerticle.springContext = context;

        // latch用于等待部署完成
        CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(HttpServerVerticle.class, depOptions, ar -> {
            // 部署失败
            if (!ar.succeeded()) {
                // 记日志
                log.error("err:", ar.cause());
                // 将异常设置到成员变量中
                this.bootError = ar.cause();
                latch.countDown();

                return;
            }

            log.info("verticle verticle deployed successfully at {}", HttpServerVerticle.port);
            latch.countDown();
        });

        try {
            latch.await();

            // 如果bootError不为空, 说明部署失败
            if (null != bootError) {
                // 关闭vertx
                vertx.close();
                // 抛出异常, 触发spring启动失败
                throw bootError;
            }

        } catch (Throwable e) {
            throw new VertxDeploymentException("failed to deploy verticle", e);
        }
    }
}
