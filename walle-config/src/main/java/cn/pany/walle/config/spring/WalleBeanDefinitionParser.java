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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by pany on 16/9/6.
 */
public class WalleBeanDefinitionParser implements BeanDefinitionParser {
    private static final Logger logger = LoggerFactory.getLogger(WalleBeanDefinitionParser.class);

    private final Class<?> beanClass;

    public WalleBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");
        if (RegistryBean.class.equals(beanClass)) {
            String address = element.getAttribute("address");
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "address", address);

        } else if (ReferenceBean.class.equals(beanClass)) {
//            String id = element.getAttribute("id");
            String interfaceName = element.getAttribute("interface");
            String version = element.getAttribute("version");

            String appName = element.getAttribute("app");
            RuntimeBeanReference appBean = new RuntimeBeanReference(appName);


            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "interfaceName", interfaceName);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "version", version);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "walleApp", appBean);
            try {
                beanDefinition.getPropertyValues().addPropertyValue(
                        "interfaceClass", Class.forName(interfaceName));
            } catch (ClassNotFoundException e) {
                logger.error("",e);
            }

        } else if (WalleAppBean.class.equals(beanClass)) {
            String appName = element.getAttribute("appName");

            String registryName = element.getAttribute("registry");
            RuntimeBeanReference registryBean = new RuntimeBeanReference(registryName);
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            beanDefinition.getPropertyValues().addPropertyValue("appName", appName);
            beanDefinition.getPropertyValues().addPropertyValue("registry", registryBean);

        } else if (WalleServiceBean.class.equals(beanClass)) {
            String interfaceName = element.getAttribute("interface");
            String implName = element.getAttribute("impl");
            String version = element.getAttribute("version");

            String appName = element.getAttribute("app");
            RuntimeBeanReference appBean = new RuntimeBeanReference(appName);


            beanDefinition.getPropertyValues().addPropertyValue("id", id);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "interfaceName", interfaceName);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "implName", implName);

            beanDefinition.getPropertyValues().addPropertyValue(
                    "version", version);
            beanDefinition.getPropertyValues().addPropertyValue(
                    "walleApp", appBean);
            try {
                beanDefinition.getPropertyValues().addPropertyValue(
                        "implClass", Class.forName(implName));
            } catch (ClassNotFoundException e) {
                logger.error("", e);
            }
        }
        parserContext.getRegistry().registerBeanDefinition(id,
                beanDefinition);

        return beanDefinition;
    }


}
