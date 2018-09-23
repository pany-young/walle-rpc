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
public class WalleAppBean implements FactoryBean<WalleApp>, ApplicationContextAware {

    // 接口类型
    private String appName;

    private transient ApplicationContext applicationContext;
    // 注册中心
//    protected List<WalleRegistry> registries;
    private RegistryBean registrie;
//    private WalleRegistry registrieBean;
    WalleApp walleApp;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public RegistryBean getRegistrie() {
        return registrie;
    }

    public void setRegistrie(RegistryBean registrie) {
        this.registrie = registrie;
    }

    @Override
    public WalleApp getObject() throws Exception {
        if(walleApp==null){
            walleApp=new WalleApp(appName, loadRegistries());
        }

        return  walleApp;
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

    protected WalleRegistry loadRegistries() throws Exception {
        //检测注册
        return registrie.getObject();
    }
//        return registry.getRegistryAddress();

}
