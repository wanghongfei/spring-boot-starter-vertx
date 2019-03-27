# Vertx Starter

Spring Boot无缝集成vertx-web框架。

## 使用方法

添加依赖:

```
<dependency>
	<groupId>cn.fh</groupId>
	<artifactId>spring-boot-starter-vertx</artifactId>
	<version>1.5-SNAPSHOT</version>
</dependency>
```

添加配置:

```yaml
server:
  port: 9000

vertx:
  # 事件循环处理线程数
  nio-thread-count: 6
  # worker线程池大小
  worker-thread-count: 10
  # HTTP Server Verticle部署数量
  verticle-count: 6
  # 允许NIO线程处理单个请求的最长时间, 毫秒
  max-event-loop-execute-time: 2000

  handler-mappings:
    # path为/demo的请求, 会依次经过homeHander, demoHandler
    - path: /demo
      method: POST
      # spring bean名
      beanNames: homeHandler,demoHandler
```

编写Handler:

```java
@Component
// @BlockedHandler
@Slf4j
public class DemoHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext route) {
        log.info("invoke DemoHandler, path: {}", route.request().path());
        route.response().end("ok");
    }
}
```

所有的Handler都会在vertx的NIO线程中执行，所以不要在handler中出现阻塞调用。

如需调用会阻塞线程的方法，可以在Handler上添加`@BlockedHandler`注解，带有此注解的Handler会在vertx的worker线程池中执行; 或者使用vertx的异步编程方式：

```java
        Future<String> fut1 = Future.future();
        Future<String> fut2 = Future.future();


        // 执行block调用
        route.vertx()
                .executeBlocking(
                        fut -> {
                            String result = demoService.blockingLogic(1);
                            fut.complete(result);
                        },
                        fut1.completer()
                );

        // 执行block调用
        route.vertx()
                .executeBlocking(
                        fut -> {
                            String result = demoService.blockingLogic(2);
                            fut.complete(result);
                        },
                        fut2.completer()
                );

        // 组合结果
        CompositeFuture.all(fut1, fut2).setHandler(ar -> {
            if (!ar.succeeded()) {
                log.error("", ar.cause());
                route.response().end("error");
                return;
            }

            log.info("final step");

            List<String> resultList = ar.result().list();
            route.response().end(resultList.toString());
        });
```

