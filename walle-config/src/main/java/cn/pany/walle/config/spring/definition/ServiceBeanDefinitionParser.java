//package cn.pany.walle.config.spring.definition;
//
//import org.springframework.beans.factory.support.BeanDefinitionBuilder;
//import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
//import org.springframework.util.StringUtils;
//import org.w3c.dom.Element;
///**
// * @author pany young
// * @email dev_pany@163.com
// * @date 2019/4/14
// */
//public class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
//
//    @SuppressWarnings("rawtypes")
//    protected Class getBeanClass(Element element) {
//        return User.class;
//    }
//
//    protected void doParse(Element element, BeanDefinitionBuilder bean) {
//        String userName = element.getAttribute("userName");
//        String email = element.getAttribute("email");
//        if (StringUtils.hasText(userName)) {
//            bean.addPropertyValue("userName", userName);
//        }
//        if (StringUtils.hasText(email)) {
//            bean.addPropertyValue("email", email);
//        }
//
//    }
//
//}
