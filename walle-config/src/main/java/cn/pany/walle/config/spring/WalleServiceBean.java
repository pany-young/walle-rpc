package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.server.WalleServerHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by pany on 16/9/6.
 */
public class WalleServiceBean<T> implements FactoryBean, ApplicationContextAware {

    protected String id;
    // 接口类型
    private String interfaceName;
    private String implName;
    private Class<?> interfaceClass;
    private Class<?> implClass;
    //class#method:version 暂不用
    //class:version
    private String version;
    private String protocol;
    private String invokerUrl;
    // 接口代理类引用
    private transient volatile Object ref;

    private transient ApplicationContext applicationContext;

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
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

    @Override
    public Object getObject() throws Exception {

        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<walle:reference interface=\"\" /> interface not allow null!");
        }
        if (interfaceClass == null) {
            interfaceClass = Class.forName(interfaceName);
        }

        Map<String, String> map = new HashMap();
        if (StringUtils.isBlank(version)) {
            version = "1.0.0";
        }
        invokerUrl = InvokerUtil.formatInvokerUrl(interfaceName, null, version);

        ref = interfaceClass.newInstance();
        String invokerUrl = InvokerUtil.formatInvokerUrl(interfaceName, null, version);

        WalleServerHandler.handlerMap.put(invokerUrl, ref);


        return ref;
    }


    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
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

}
