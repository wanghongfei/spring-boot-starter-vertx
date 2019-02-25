package cn.fh.springboot.starter.vertx.error;

/**
 * Created by wanghongfei on 2019-02-25.
 */
public class VertxDeploymentException extends RuntimeException {
    /**
     * 包含message和cause, 会记录栈异常
     * @param msg
     * @param cause
     */
    public VertxDeploymentException(String msg, Throwable cause) {
        super(msg, cause, false, true);
    }
}
