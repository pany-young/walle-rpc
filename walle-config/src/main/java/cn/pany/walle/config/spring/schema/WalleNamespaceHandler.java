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
package cn.pany.walle.config.spring.schema;

import cn.pany.walle.config.spring.ReferenceBean;
import cn.pany.walle.config.spring.RegistryBean;
import cn.pany.walle.config.spring.WalleAppBean;
import cn.pany.walle.config.spring.WalleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by pany on 16/9/6.
 */
public class WalleNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("registry", new WalleBeanDefinitionParser(RegistryBean.class));
        registerBeanDefinitionParser("app", new WalleBeanDefinitionParser(WalleAppBean.class));
        registerBeanDefinitionParser("reference", new WalleBeanDefinitionParser(ReferenceBean.class));
    }
}
