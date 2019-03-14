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
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by pany on 16/9/6.
 */
public class ReferenceBean<T> implements FactoryBean, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(ReferenceBean.class);

    protected String id;
    // 接口类型
    private String interfaceName;

    private Class<?> interfaceClass;

    //class#method:version 暂不用
    //class:version
    private String version;
    private String protocol;
    private String invokerUrl;
    // 接口代理类引用
    private transient volatile T ref;

    private transient ApplicationContext applicationContext;
    // 注册中心
//    protected List<WalleRegistry> registries;
    private WalleApp walleApp;
    private transient volatile WalleInvoker<?> invoker;


    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public Object getObject() throws Exception {

        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<walle:reference interface=\"\" /> interface not allow null!");
        }
        if (interfaceClass == null) {
            interfaceClass = Class.forName(interfaceName);
        }

        Map<String, String> map = new HashMap();
        if (StringUtils.isBlank(version)) {
            version = "1.0.0";
        }
        invokerUrl = InvokerUtil.formatInvokerUrl(interfaceName, null, version);


//        invokerUrl =  interfaceName +":"+ version;
        invoker = WalleInvoker.walleInvokerMap.get(invokerUrl);
        if (invoker == null) {
            invoker = new WalleInvoker<>(interfaceClass, invokerUrl);
            WalleInvoker checkInvoker = WalleInvoker.walleInvokerMap.putIfAbsent(invokerUrl, invoker);
            if (checkInvoker != null) {
                invoker = checkInvoker;
            }
        }

        map.put(WalleConstant.INTERFACE_CLASS_KEY, interfaceName);

        if (ref == null) {
            ref = createProxy(map);
        }

        return ref;
    }

    private T createProxy(Map<String, String> map) {

        //如无初始化则进行初始化
        //从zookeeper获取服务端的信息
        //进行连接
        //对所需接口
        if (walleApp.init()) {

            ref = (T) WalleProxy.create(map, invoker);
            // 创建服务代理
            return ref;
        } else {
            return null;
        }


    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    protected boolean checkRegistry() {
        if (walleApp != null) {
            return walleApp.getWalleRegistry() == null ? true : false;
        } else {
            return false;
        }
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getInvokerUrl() {
        return invokerUrl;
    }

    public void setInvokerUrl(String invokerUrl) {
        this.invokerUrl = invokerUrl;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }

    public WalleInvoker<?> getInvoker() {
        return invoker;
    }

    public void setInvoker(WalleInvoker<?> invoker) {
        this.invoker = invoker;
    }

}
