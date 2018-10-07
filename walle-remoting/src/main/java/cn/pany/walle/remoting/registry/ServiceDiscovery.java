//package cn.pany.walle.remoting.registry;
//
//import cn.pany.walle.common.constants.NettyConstant;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.apache.curator.framework.api.GetChildrenBuilder;
//import org.apache.curator.framework.recipes.cache.PathChildrenCache;
//import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
//import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
//import org.apache.curator.retry.RetryNTimes;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
///**
// * Created by pany on 16/9/3.
// */
//@Slf4j
//public class ServiceDiscovery {
//
////    private CountDownLatch latch = new CountDownLatch(1);
//
//    private volatile List<String> dataList = new ArrayList<String>();
//
//    public List<String> getDataList() {
//        return dataList;
//    }
//
//    private String registryAddress;
//
//    public ServiceDiscovery(String registryAddress) {
//        this.registryAddress = registryAddress;
//
//        CuratorFramework curatorFramework = connectServer();
//        if (curatorFramework != null) {
//            watchNode(curatorFramework);
//        }
//
//    }
//
//    public String discover() {
//        String data = null;
//        int size = dataList.size();
//        if (size > 0) {
//            if (size == 1) {
//                data = dataList.get(0);
//                log.debug("using only data: {}", data);
//            } else {
//                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
//                log.debug("using random data: {}", data);
//            }
//        }
//        return data;
//    }
//
//    private CuratorFramework connectServer() {
//        CuratorFramework client = CuratorFrameworkFactory
//                .builder()
//                .connectString(registryAddress)
//                .namespace(NettyConstant.ZK_REGISTRY_PATH)
//                .retryPolicy(new RetryNTimes(2000, 20000))
//                .build();
//
//        client.start();
//        return client;
//    }
//
//
//    private void watchNode(final CuratorFramework client) {
//        try {
//           PathChildrenCache childrenCache = new PathChildrenCache(client,"/", true);
//            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
//                @Override
//                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//                    log.info("节点子:-----");
//                    switch (event.getType()) {
//                        case CHILD_ADDED:
//                        case CHILD_REMOVED:
//                        case CHILD_UPDATED:
//                            updateDataList(client);
//                        default:
//                            break;
//                    }
//                }
//            };
//            childrenCache.getListenable().addListener(childrenCacheListener);
//            log.info("Register zk watcher successfully!");
//            childrenCache.start(PathChildrenCache.StartMode.NORMAL);
//
//            updateDataList(client);
////            this.dataList = dataList;
//        } catch (Exception e) {
//            log.error( e.getMessage());
//        }
//    }
//
//    public void updateDataList(final CuratorFramework client) throws Exception{
//        GetChildrenBuilder getChildrenBuilder = client.getChildren();
//        dataList = getChildrenBuilder.forPath("/");
//        log.debug("node data: {}", dataList);
//    }
//
//}
