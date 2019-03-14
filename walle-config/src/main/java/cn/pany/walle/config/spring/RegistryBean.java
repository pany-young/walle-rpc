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

import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.registry.WalleRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by pany on 18/9/21.
 */
public class RegistryBean implements FactoryBean<WalleRegistry>, ApplicationContextAware {
    private transient ApplicationContext applicationContext;
    protected String id;
    // 注册中心
    private String address;

    private WalleRegistry walleRegistry;


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public WalleRegistry getObject() throws Exception {
        if(walleRegistry == null){
            walleRegistry=new WalleRegistry(address);
            walleRegistry.register();
        }
        return  walleRegistry;
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
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
