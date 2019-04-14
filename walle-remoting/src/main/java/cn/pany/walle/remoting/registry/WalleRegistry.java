/*
 * Copyright 2018-2019 Pany Young.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.pany.walle.remoting.registry;

import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.model.ServerInfo;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pany on 16/8/25.l
 */
public class WalleRegistry {
    private static final Logger log = LoggerFactory.getLogger(WalleRegistry.class);

    //    private CountDownLatch latch = new CountDownLatch(1);
    public final static String ZK_SPLIT = "/";
    public final static String WALLE_SERVER_DEFULT = "server";
//    public static String INIT_PATH = "/cn_pany/walle";

    private CuratorFramework client;

    private String registryAddress;
    private volatile String registryState;

    public WalleRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public CuratorFramework register(String registryAddress) throws Exception {
        this.registryAddress = registryAddress;
        if (client == null) {
            this.client = connectServer();
        }

        return this.client;
    }

    public CuratorFramework register() throws Exception {
        if (client == null) {
            this.client = connectServer();
        }
        return this.client;
    }


    //连接zookpeer
    private synchronized CuratorFramework connectServer() throws Exception {
        if (client == null) {
            client = CuratorFrameworkFactory
                    .builder()
                    .connectString(registryAddress)
                    .namespace(WalleConstant.NAME_SPACE)
                    .retryPolicy(new RetryNTimes(2000, 20000))
                    .build();
        }
        if (client.getState() != CuratorFrameworkState.STARTED) {
            client.start();
//            createInitNode(client);
        }

        return client;
    }

    public void addServiceListener(Map<String, Set<InterfaceDetail>> serverAppMap, String serverAddress) throws Exception {
        try {
            setServerInfo(serverAppMap,serverAddress);
            ConnectionStateListener connectionStateListener = new ConnectionStateListener() {
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    if (ConnectionState.RECONNECTED == newState) {
                        setServerInfo(serverAppMap,serverAddress);
                    }else if (ConnectionState.CONNECTED == newState) {
                        setServerInfo(serverAppMap,serverAddress);
                    }
                }
            };

            register().getConnectionStateListenable().addListener(connectionStateListener);


        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void setServerInfo(Map<String, Set<InterfaceDetail>> serverAppMap, String serverAddress){
        for (Map.Entry<String, Set<InterfaceDetail>> tempApp : serverAppMap.entrySet()) {
            Set<InterfaceDetail> interfaceDetailSet = tempApp.getValue();
            if (interfaceDetailSet == null || interfaceDetailSet.isEmpty()) {
                continue;
            }
            String appName = tempApp.getKey();
            String appPath = WalleRegistry.ZK_SPLIT + appName + WalleRegistry.ZK_SPLIT + WalleRegistry.WALLE_SERVER_DEFULT + WalleRegistry.ZK_SPLIT + serverAddress;

            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setInterfaceDetailSet(interfaceDetailSet);
            try {
                setData(appPath, JSON.toJSONString(serverInfo));
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }
    public void removeServiceListener(Map<String, Set<InterfaceDetail>> serverAppMap, String serverAddress) throws Exception {
        for (Map.Entry<String, Set<InterfaceDetail>> tempApp : serverAppMap.entrySet()) {
            Set<InterfaceDetail> interfaceList = tempApp.getValue();
            if (interfaceList == null || interfaceList.isEmpty()) {
                continue;
            }
            String appName = tempApp.getKey();
            String appPath = WalleRegistry.ZK_SPLIT + appName + WalleRegistry.ZK_SPLIT + WalleRegistry.WALLE_SERVER_DEFULT + WalleRegistry.ZK_SPLIT + serverAddress;

            register().delete().forPath(appPath);
        }
    }

    public void createPersistentNode(String path) throws Exception {
        register().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).
                forPath(path);
    }

    public void createEphemeralNode(String path) throws Exception {
        register().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).
                forPath(path);
    }

    private void setDataNode(String path, String data) throws Exception {
        register().create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).
                forPath(path, (data).getBytes());
    }

    public void setData(String path, String data) throws Exception {
        if (data != null) {
            setDataNode(path, data);
        }
    }

    public byte[] getData(String path) throws Exception {
        return register().getData().forPath(path);
    }

    public List<String> getChildrenList(String path) throws Exception {
        return register().getChildren().forPath(path);
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public boolean isRegister() {
        if (client == null) {
            return false;
        }
        if (client.getState() != CuratorFrameworkState.STARTED) {
            return false;
        }
        return true;
    }
}
