package cn.pany.walle.config.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by pany on 16/9/6.
 */
public class ValyBeanDefinitionParser implements BeanDefinitionParser {
    private static final Logger logger = LoggerFactory.getLogger(ValyBeanDefinitionParser.class);

    private final Class<?> beanClass;

    public ValyBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");
        String interfaceName = element.getAttribute("interface");

        beanDefinition.getPropertyValues().addPropertyValue("id", id);
        beanDefinition.getPropertyValues().addPropertyValue(
                "interfaceName", interfaceName);

        parserContext.getRegistry().registerBeanDefinition(id,
                beanDefinition);


        return beanDefinition;
    }


}
