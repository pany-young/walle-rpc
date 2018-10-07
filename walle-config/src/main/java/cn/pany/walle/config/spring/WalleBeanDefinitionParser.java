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

        }
        parserContext.getRegistry().registerBeanDefinition(id,
                beanDefinition);
        return beanDefinition;
    }


}
