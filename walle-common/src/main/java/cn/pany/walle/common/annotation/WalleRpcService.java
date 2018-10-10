package cn.pany.walle.common.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pany on 16/8/25.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Service() // 表明可被 Spring 扫描
public @interface WalleRpcService {

    Class<?> value();
    String appName();
    String version() default "1.0.0";
}