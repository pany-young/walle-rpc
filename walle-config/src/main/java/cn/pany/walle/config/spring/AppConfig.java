//package cn.pany.walle.config.spring;
//
//import cn.pany.walle.remoting.api.WalleApp;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.FactoryBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//
///**
// * Created by pany on 18/9/13.
// */
//public class AppConfig <T> implements FactoryBean,ApplicationContextAware {
//
//    private transient ApplicationContext applicationContext;
//    // 应用名
//    private String appName;
//
//    @Override
//    public Object getObject() throws Exception {
//        return null;
//    }
//
//    @Override
//    public Class<?> getObjectType() {
////        return getInterfaceClass();
//
//        return WalleApp.class;
//    }
//
//    @Override
//    public boolean isSingleton() {
//        return true;
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext=applicationContext;
//    }
//}
