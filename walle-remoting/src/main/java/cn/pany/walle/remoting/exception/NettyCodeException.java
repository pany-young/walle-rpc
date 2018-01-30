package cn.pany.walle.remoting.exception;

/**
 * Created by pany on 16/9/5.
 */
public class NettyCodeException extends RuntimeException {

    public NettyCodeException(String message) {
        super(message);
    }

    public NettyCodeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
