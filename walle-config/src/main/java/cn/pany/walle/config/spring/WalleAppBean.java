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
package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.model.ServerInfo;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.common.utils.StringUtils;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.registry.WalleRegistry;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by pany on 18/9/21.
 */
public class WalleAppBean implements FactoryBean<WalleApp>, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(WalleAppBean.class);

    private String id;
    // 接口类型
    private String appName;
    private String version;

    private transient ApplicationContext applicationContext;
    // 注册中心
//    protected List<WalleRegistry> registries;
//    private RegistryBean registrie;
    private WalleRegistry registry;
    private WalleApp walleApp;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public WalleApp getObject() throws Exception {
        if (walleApp == null) {
            walleApp = new WalleApp(appName, loadRegistries());
            walleApp.init();
        }

        return walleApp;
    }

    @Override
    public Class<?> getObjectType() {
        return WalleApp.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

//        log.info("Register zk watcher begin!");

    }

    protected WalleRegistry loadRegistries() throws Exception {
        //检测注册
        return registry;
    }
//        return registry.getRegistryAddress();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public WalleRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(WalleRegistry registry) {
        this.registry = registry;
    }

    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }
}
