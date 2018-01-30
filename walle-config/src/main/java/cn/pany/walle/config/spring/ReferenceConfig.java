package cn.pany.walle.config.spring;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.constants.Constants;
import cn.pany.walle.common.constants.NettyConstant;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.registry.WalleRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pany on 16/9/6.
 */
public class ReferenceConfig<T> implements FactoryBean,ApplicationContextAware {

    protected String id;
    // 接口类型
    private String interfaceName;
    private Class<?>             interfaceClass;

    // 接口代理类引用
    private transient volatile T ref;

    private transient ApplicationContext applicationContext;
    // 注册中心
    protected List<WalleRegistry> registries;

    // 接口代理类引用
    private transient volatile T ref;
    private transient volatile Invoker<?> invoker;
    private final List<URL> urls = new ArrayList<URL>();

    ValyProxy valyProxy;

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
        Map<String,String> map= new HashMap();



//        ValyRpcProxy valyRpcProxy = (ValyRpcProxy) this.applicationContext.getBean("valyRpcProxy");
//        Object consumerProxy = valyRpcProxy.create(intefaceClazz);
        map.put(NettyConstant.INTERFACE_CLASS_KEY,interfaceName);

//        Object consumerProxy = valyProxy.create(map);

        return createProxy(map);
    }

    private T createProxy(Map<String, String> map) {
//        List<URL> us = loadRegistries(false);
        //通过注册器查找对应客户端

        if (us != null && us.size() > 0) {
            for (URL u : us) {
                urls.add(u);
            }
        }
        if (urls == null || urls.size() == 0) {
            throw new IllegalStateException("No such any registry to reference " + interfaceName + " on the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", please config <dubbo:registry address=\"...\" /> to your spring config.");
        }

        if (urls.size() == 1) {
            invoker = refprotocol.refer(interfaceClass, urls.get(0));
        } else {
            List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
            URL registryURL = null;
            for (URL url : urls) {
                invokers.add(refprotocol.refer(interfaceClass, url));
                if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                    registryURL = url; // 用了最后一个registry url
                }
            }
            if (registryURL != null) { // 有 注册中心协议的URL
                // 对有注册中心的Cluster 只用 AvailableCluster
                URL u = registryURL.addParameter(Constants.CLUSTER_KEY, AvailableCluster.NAME);
                invoker = cluster.join(new StaticDirectory(u, invokers));
            } else { // 不是 注册中心的URL
                invoker = cluster.join(new StaticDirectory(invokers));
            }
        }


        ref=(T) valyProxy.create(map);
        // 创建服务代理
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
        this.applicationContext=applicationContext;
    }

    protected List<URL> loadRegistries(boolean provider) {
        //检测注册
        checkRegistry();
        List<URL> registryList = new ArrayList<URL>();
        if (registries != null && registries.size() > 0) {
            for (WalleRegistry registry : registries) {
                String address = registry.getRegistryAddress();
                if (address == null || address.length() == 0) {
                    address = Constants.ANYHOST_VALUE;
                }

                String sysaddress = System.getProperty("walle.registry.address");
                if (sysaddress != null && sysaddress.length() > 0) {
                    address = sysaddress;
                }
                if (address != null && address.length() > 0) {
                    Map<String, String> map = new HashMap<String, String>();
                          map.put("protocol", "remote");

                     registryList = UrlUtils.parseURLs(address, map);

                }
            }
        }
        return registryList;
    }

    protected void checkRegistry() {

        if ((registries == null || registries.size() == 0)) {
            throw new IllegalStateException((getClass().getSimpleName().startsWith("Reference")
                    ? "No such any registry to refer service in consumer "
                    : "No such any registry to export service in provider ")
                    + NetUtils.getLocalHost()
                    + ", Please add <dubbo:registry address=\"...\" /> to your spring config. If you want unregister, please set <dubbo:service registry=\"N/A\" />");
        }
    }
}
