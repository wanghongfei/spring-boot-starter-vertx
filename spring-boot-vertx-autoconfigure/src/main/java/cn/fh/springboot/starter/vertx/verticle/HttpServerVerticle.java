package cn.fh.springboot.starter.vertx.verticle;

import cn.fh.springboot.starter.vertx.VertxProperties;
import cn.fh.springboot.starter.vertx.meta.BlockedHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Slf4j
public class HttpServerVerticle extends AbstractVerticle {
    protected static ApplicationContext springContext;

    protected static int port = 8080;

    @Override
    public void start(Future<Void> startFuture) {
        // 取出配置信息
        VertxProperties config = springContext.getBean(VertxProperties.class);
        Integer port = springContext.getEnvironment().getProperty("server.port", Integer.class);
        if (null != port) {
            HttpServerVerticle.port = port;
        }

        // 构造server
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        addHandlers(router, config.getHandlerMappings());

        server.requestHandler(router)
                .listen(HttpServerVerticle.port, res -> {
                    if (res.succeeded()) {
                        startFuture.complete();

                    } else {
                        startFuture.fail(res.cause());
                    }
                });
    }

    /**
     * 添加请求处理器
     * @param router
     * @param handlerMappings
     */
    private void addHandlers(Router router, List<VertxProperties.HandlerMapping> handlerMappings) {
        // 从配置文件中取出bean名和对应的URL, Method
        for (VertxProperties.HandlerMapping hm : handlerMappings) {
            for (String beanName : hm.getBeanNames()) {
                Handler<RoutingContext> handler = (Handler<RoutingContext>) springContext.getBean(beanName);

                Route route = router.route(hm.getPath());
                if (isBlocked(handler)) {
                    route.blockingHandler(handler);

                } else {
                    route.handler(handler);
                }

            }
        }
    }

    private boolean isBlocked(Handler<?> handler) {
        BlockedHandler an = handler.getClass().getAnnotation(BlockedHandler.class);
        return an != null;
    }
}
