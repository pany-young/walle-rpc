package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.model.ServerInfo;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.registry.WalleRegistry;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by pany on 18/9/21.
 */
public class WalleAppBean implements FactoryBean<WalleApp>, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(WalleAppBean.class);

    private String id;
    // 接口类型
    private String appName;
    private String version;

    private transient ApplicationContext applicationContext;
    // 注册中心
//    protected List<WalleRegistry> registries;
//    private RegistryBean registrie;
    private WalleRegistry registry;
    private WalleApp walleApp;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public WalleApp getObject() throws Exception {
        if(walleApp==null){
            walleApp=new WalleApp(appName, loadRegistries());
        }
        walleApp.init();
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

//        log.info("Register zk watcher begin!");
        PathChildrenCache childrenCache = null;
        try {
            childrenCache = new PathChildrenCache(registry.register(), WalleRegistry.ZK_SPLIT + getObject().getAppName()+WalleRegistry.ZK_SPLIT + WalleRegistry.WALLE_SERVER_DEFULT, true);

            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    log.info("zk监听开始进行事件分析");
                    ChildData data = event.getData();
                    ServerInfo serverInfo;
                    String lastPath ;
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            serverInfo=JSON.parseObject(new String(data.getData()), ServerInfo.class);
//                            data.getPath().substring(data.getPath().lastIndexOf(WalleRegistry.ZK_SPLIT));
                            lastPath=data.getPath().substring(data.getPath().lastIndexOf(WalleRegistry.ZK_SPLIT)+1);
                            log.info("CHILD_ADDED : " + data.getPath() + "  数据:" + new String(data.getData()));

                            WalleClient walleClient=new WalleClient(getObject(), UrlUtils.parseURL(lastPath,null),serverInfo.getInterfaceDetailList(),registry);
                            if(!walleApp.getWalleClientSet().contains(walleClient)){
                                walleClient.doOpen();
                                walleApp.getWalleClientSet().add(walleClient);
                                for(InterfaceDetail interfaceDetail : serverInfo.getInterfaceDetailList()){
                                    String invokerUrl = interfaceDetail.getClassName()+interfaceDetail.getVersion();
                                    WalleInvoker walleInvoker =WalleInvoker.walleInvokerMap.get(invokerUrl);
                                    if(walleInvoker==null){
                                        walleInvoker = new WalleInvoker<>(interfaceDetail.getClass(),invokerUrl);
                                    }

                                    if(!walleInvoker.getClients().contains(walleClient)){
                                        walleInvoker.addToClients(walleClient);
                                    }
                                }
                            }

                            break;
                        case CHILD_REMOVED:
//                            serverInfo=JSON.parseObject(new String(data.getData()), ServerInfo.class);
                            lastPath=data.getPath().substring(data.getPath().lastIndexOf(WalleRegistry.ZK_SPLIT)+1);

                            log.info("CHILD_REMOVED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            log.info("CHILD_REMOVED : " + data.getPath() + "  last path:" + lastPath);

                            for(WalleClient walleClientTemp :walleApp.getWalleClientSet()){
                                if( walleClientTemp.getUrl().getAddress().equals(lastPath)){
                                    log.info("client REMOVED : " + walleClientTemp.getUrl().getAddress());
                                    walleClientTemp.close();
                                    walleApp.getWalleClientSet().remove(walleClientTemp);
                                }
                            }
                            break;
                        case CHILD_UPDATED:
//                            serverInfo=JSON.parseObject(new String(data.getData()), ServerInfo.class);
                            log.info("CHILD_UPDATED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            break;
                        default:
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener);
            log.info("Register zk app watcher path:[{}] successfully!", WalleRegistry.ZK_SPLIT+ WalleConstant.NAME_SPACE+WalleRegistry.ZK_SPLIT + getObject().getAppName());
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            log.error("",e);
        }
    }

    protected WalleRegistry loadRegistries() throws Exception {
        //检测注册
        return registry;
    }
//        return registry.getRegistryAddress();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public WalleRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(WalleRegistry registry) {
        this.registry = registry;
    }

    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }
}
