package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.common.utils.StringUtils;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.server.WalleServerHandler;
import cn.pany.walle.remoting.server.WalleSmartServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 2019/4/13
 */
public class WalleServiceBean<T> implements FactoryBean, InitializingBean, ApplicationContextAware {

    protected String id;
    // 接口类型
    private String interfaceName;
    private String implName;
//    private Class<?> interfaceClass;
    private Class<?> implClass;
    //class#method:version 暂不用
    //class:version
    private String version;
    private String protocol;
    private String invokerUrl;
    // 具体实现类引用
    private transient volatile Object ref;
    private WalleApp walleApp;
    private transient ApplicationContext applicationContext;

//    public Class<?> getInterfaceClass() {
//        return interfaceClass;
//    }

    public Class<?> getImplClass() {
        return implClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getImplName() {
        return implName;
    }

    public void setImplName(String implName) {
        this.implName = implName;
    }

    public void setImplClass(Class<?> implClass) {
        this.implClass = implClass;
    }

    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }

    @Override
    public Object getObject() throws Exception {

        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<walle:service interface=\"\" /> interface not allow null!");
        }
        if (implName == null || implName.length() == 0) {
            throw new IllegalStateException("<walle:service impl=\"\" /> interface not allow null!");
        }
        if (implClass == null) {
            implClass = Class.forName(implName);
        }

        Map<String, String> map = new HashMap();
        if (StringUtils.isBlank(version)) {
            version = "1.0.0";
        }
        invokerUrl = InvokerUtil.formatInvokerUrl(interfaceName, null, version);

        if(ref ==null){
            ref = implClass.newInstance();
            InterfaceDetail interfaceDetail = new InterfaceDetail(interfaceName, version);
            WalleServerHandler.handlerMap.put(invokerUrl, ref);

            WalleSmartServer.addInterfaceDetail(walleApp.getAppName(),interfaceDetail);
        }

        return ref;
    }


    @Override
    public Class<?> getObjectType() {
        return getImplClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getInvokerUrl() {
        return invokerUrl;
    }

    public void setInvokerUrl(String invokerUrl) {
        this.invokerUrl = invokerUrl;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
       if(ref==null){
           ref=getObject();
       }
        System.out.println("after service");
    }
}
