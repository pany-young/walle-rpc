package cn.pany.walle.demo.server.service;

import cn.pany.walle.common.annotation.WalleService;

/**
 * Created by pany on 16/8/25.
 */
@WalleService(appName = "demo-server",value = Demo1Service.class) // 指定远程接口
public class Demo1ServiceImpl implements Demo1Service {

    @Override
    public String demoOne1(String name) {
        return "DemoOne1! " + name;
    }
}