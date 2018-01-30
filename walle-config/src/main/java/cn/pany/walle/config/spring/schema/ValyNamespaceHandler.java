package cn.pany.walle.config.spring.schema;

import cn.yang.test.dubbo.config.spring.ReferenceConfig;
import cn.yang.test.dubbo.config.spring.ValyBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by pany on 16/9/6.
 */
public class ValyNamespaceHandler   extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new ValyBeanDefinitionParser(ReferenceConfig.class));
    }
}
