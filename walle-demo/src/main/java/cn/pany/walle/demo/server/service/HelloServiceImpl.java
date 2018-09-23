package cn.pany.walle.demo.server.service;

import cn.pany.walle.common.annotation.WalleService;

/**
 * Created by pany on 16/8/25.
 */
@WalleService(appName = "demo-server",value = HelloService.class) // 指定远程接口
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}