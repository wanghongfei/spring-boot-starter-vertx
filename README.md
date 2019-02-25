# Vertx Starter

## 使用方法

添加依赖:

```
<dependency>
	<groupId>cn.fh</groupId>
	<artifactId>spring-boot-starter-vertx</artifactId>
	<version>1.0-SNAPSHOT</version>
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
      # spring bean名
      beanNames: homeHandler,demoHandler
```

编写Handler:

```java
@Component
@Slf4j
public class DemoHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext route) {
        log.info("invoke DemoHandler, path: {}", route.request().path());
        route.response().end("ok");
    }
}
```

