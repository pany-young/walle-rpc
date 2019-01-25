package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
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
public class ReferenceBean<T> implements FactoryBean, ApplicationContextAware {

    protected String id;
    // 接口类型
    private String interfaceName;
    private Class<?> interfaceClass;
    //class#method:version 暂不用
    //class:version
    private String version;
    private String protocol;
    private String invokerUrl;
    // 接口代理类引用
    private transient volatile T ref;

    private transient ApplicationContext applicationContext;
    // 注册中心
//    protected List<WalleRegistry> registries;
    private WalleApp walleApp;
    private transient volatile WalleInvoker<?> invoker;


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
        if(interfaceClass == null){
            interfaceClass = Class.forName(interfaceName);
        }

        Map<String, String> map = new HashMap();
        if(StringUtils.isBlank(version)){
            version= "1.0.0";
        }
        invokerUrl= InvokerUtil.formatInvokerUrl(interfaceName,null,version);;

//        invokerUrl =  interfaceName +":"+ version;
        invoker =WalleInvoker.walleInvokerMap.get(invokerUrl);
        if(invoker == null){
            invoker = new WalleInvoker<>(interfaceClass, invokerUrl);
        }

//        ValyRpcProxy valyRpcProxy = (ValyRpcProxy) this.applicationContext.getBean("valyRpcProxy");
//        Object consumerProxy = valyRpcProxy.create(intefaceClazz);
        map.put(WalleConstant.INTERFACE_CLASS_KEY, interfaceName);

//        Object consumerProxy = valyProxy.create(map);
        if(ref==null){
            ref=createProxy(map);
        }

        return ref;
    }

    private T createProxy(Map<String, String> map) {

        //如无初始化则进行初始化
        //从zookeeper获取服务端的信息
        //进行连接
        //对所需接口
        if(walleApp.init()){
            Set<WalleClient> walleClientSet = walleApp.getWalleClientSet();

//            if (walleClientSet == null || walleClientSet.size() == 0) {
//                throw new IllegalStateException("No such any client on app:" + walleApp.getAppName() + ".");
//            }

            if (walleClientSet != null  ) {
                //从client里面匹配接口
                for (WalleClient client : walleClientSet) {
                    //class#method:version
                    WalleClient walleClient= client.getInterfaceMap().get(invokerUrl);
                    if(walleClient != null){
                        invoker.addToClients(walleClient);
                    }
                }
            }


            ref = (T) WalleProxy.create(map, invoker);
            // 创建服务代理
            return ref;
        }else {
            return null;
        }



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


    protected boolean checkRegistry() {
        if (walleApp != null) {
            return walleApp.getWalleRegistry() == null ? true : false;
        } else {
            return false;
        }
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

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }

    public WalleInvoker<?> getInvoker() {
        return invoker;
    }

    public void setInvoker(WalleInvoker<?> invoker) {
        this.invoker = invoker;
    }

}
