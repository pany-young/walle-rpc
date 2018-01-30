package cn.pany.walle.remoting.registry;

import cn.pany.walle.common.constants.NettyConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

/**
 * Created by pany on 16/8/25.
 */
@Slf4j
public class WalleRegistry {
//    private static final Logger LOGGER = LoggerFactory.getLogger(WalleRegistry.class);

//    private CountDownLatch latch = new CountDownLatch(1);

    public static String INIT_PATH="walle";

    private CuratorFramework client;

    private String registryAddress;
    private volatile String registryState;

    public WalleRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register() throws Exception {
            this.client = connectServer();
    }

    public void setData(String path,String data) throws Exception {
        if (data != null) {
            CuratorFramework client = connectServer();
            if (client != null) {
                createNode(path, data);
            }
        }
    }

    //连接zookpeer
    private synchronized CuratorFramework connectServer() throws Exception {
        if(client==null){
            client = CuratorFrameworkFactory
                .builder()
                .connectString(registryAddress)
                .namespace(NettyConstant.NAME_SPACE)
                .retryPolicy(new RetryNTimes(2000, 20000))
                .build();
        }
        if(client.getState() != CuratorFrameworkState.STARTED){
            client.start();
        }
        createInitNode(client);
        return client;
    }


    private void createInitNode(CuratorFramework client) throws Exception {
        client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT)//存储类型（临时的还是持久的）
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)//访问权限
                .forPath("/" + INIT_PATH);//创建的路径

    }

    private void createNode(String path,String data) throws Exception {
        client.//对路径节点赋值
                setData().
                forPath("/" + path, (data).getBytes());

    }
    public List<String> getData(String path) throws Exception {

        return client.getChildren().forPath(path);
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public boolean isRegister(){
        if(client==null){
            return false;
        }
        if(client.getState() != CuratorFrameworkState.STARTED){
            return false;
        }
        return true;
    }
}
