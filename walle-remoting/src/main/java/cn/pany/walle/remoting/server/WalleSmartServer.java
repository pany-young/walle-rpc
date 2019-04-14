/*
 * Copyright 2018-2019 Pany Young.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.pany.walle.remoting.server;

import cn.pany.walle.common.annotation.WalleRpcService;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.utils.ExecutorUtil;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.remoting.registry.WalleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 2019/1/22
 */
//给springboot的使用的server
public class WalleSmartServer extends WalleServer implements ApplicationContextAware, SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(WalleSmartServer.class);

    private ApplicationContext ctx;
    private boolean isRunning = false;
    private static ExecutorService executorService = ExecutorUtil.getSineleThreadExecutor();

    public WalleSmartServer(int port, WalleRegistry walleRegistry) throws UnknownHostException {
        super(port, walleRegistry);
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        log.info("stop by Runnable");
        try {
            if (serverAppMap != null && getServerAddress() != null) {

                getWalleRegistry().removeServiceListener(serverAppMap, getServerAddress());

            }
            log.info("removeServiceListener ok");
//        future.channel().closeFuture().syncUninterruptibly();
//        log.info("channel close");
            shutdown();
            log.info("walleServer workerGroup & bossGroup shutdown");

            executorService.shutdown();
            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            log.info("executorService shutdown");
        } catch (Exception e) {
            log.error("", e);
        }
        runnable.run();
        isRunning = false;
    }

    @Override
    public void start() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                bind();
            }
        });
        registry();
// 执行完其他业务后，可以修改 isRunning = true
        isRunning = true;
    }

    @Override
    public void stop() {
        log.info("stop no with other");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return 1;
    }

    //Spring时，扫描写入
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

//    @Override
//    public void destroy() throws Exception {
//
//    }

    //appName,InterfaceList
    public static Map<String, Set<InterfaceDetail>> serverAppMap = new ConcurrentHashMap<>();

    private void registry() {

        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(WalleRpcService.class); // 获取所有带有 WalleRpcService 注解的 Spring Bean
        if (!serviceBeanMap.isEmpty()) {
//            if(serverAppMap==null){
//                serverAppMap = new HashMap<>();
//            }
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(WalleRpcService.class).value().getName();
                String version = serviceBean.getClass().getAnnotation(WalleRpcService.class).version();
                String appName = serviceBean.getClass().getAnnotation(WalleRpcService.class).appName();
                String invokerUrl = InvokerUtil.formatInvokerUrl(interfaceName, null, version);

                WalleServerHandler.handlerMap.put(invokerUrl, serviceBean);

                //注册到zookpeer
                //class#method:version
                InterfaceDetail interfaceDetail = new InterfaceDetail(interfaceName, version);
                addInterfaceDetail(appName, interfaceDetail);
            }
        }
        try {
            getWalleRegistry().addServiceListener(serverAppMap, getServerAddress());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static void addInterfaceDetail(String appName, InterfaceDetail interfaceDetail) {
//注册到zookpeer
        //class#method:version
//        InterfaceDetail interfaceDetail = new InterfaceDetail(interfaceName, version);
        Set<InterfaceDetail> interfaceList = new HashSet<>();
        Set<InterfaceDetail> checkList = serverAppMap.putIfAbsent(appName, interfaceList);
        if (checkList == null) {
//                    interfaceList.add(interfaceName + ":" + version);
            interfaceList.add(interfaceDetail);
//                    serverAppMap.put(appName, interfaceList);
        } else {
            checkList.add(interfaceDetail);
        }
    }
}
