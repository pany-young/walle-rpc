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
