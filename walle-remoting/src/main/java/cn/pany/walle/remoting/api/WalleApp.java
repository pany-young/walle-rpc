package cn.pany.walle.remoting.api;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.model.ServerInfo;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.registry.WalleRegistry;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pany on 18/1/22.
 */
@Slf4j
public class WalleApp {
    private String appName;

    private Set<WalleClient> walleClientSet=new HashSet<>();

    private WalleRegistry walleRegistry;
    private  AppState appState=AppState.INIT;
    private  String version;
    private String appPath;
    enum AppState{
        INIT(1),INITED(2),CLOSE(9);

        private int code;
        AppState(int code){this.code=code;}
    }

    public WalleApp(String appName, WalleRegistry walleRegistry) {
        this.walleRegistry = walleRegistry;
        this.appName = appName;
    }
    public WalleApp(String appName, WalleRegistry walleRegistry,String version) {
        this.walleRegistry = walleRegistry;
        this.appName = appName;
        this.version= version;
    }
    /*从zookeeper获取服务端的信息
    进行连接
    对所需接口*/
    public synchronized boolean init()  {
        if(appState==AppState.INITED){
            return true;
        }else if(appState==AppState.CLOSE){
            return false;
        }

        try {
            if (walleRegistry.isRegister()) {
                walleRegistry.register();
            }
            appPath=WalleRegistry.ZK_SPLIT + appName +WalleRegistry.ZK_SPLIT + WalleRegistry.WALLE_SERVER_DEFULT ;
            List<String> serverList = walleRegistry.getChildrenList(appPath);

            //ip:port#version@protocol
            for (String serverDetail : serverList) {
                URL url = UrlUtils.parseURL(serverDetail, null);

                byte[] interfaceListByte = walleRegistry.
                getData(appPath + WalleRegistry.ZK_SPLIT + serverDetail);
                List<InterfaceDetail> interfaceList =
                        JSON.parseObject(new String(interfaceListByte), ServerInfo.class).getInterfaceDetailList();

                WalleClient walleClient = new WalleClient(this,url,interfaceList,walleRegistry);
                if(!walleClientSet.contains(walleClient)){
                    walleClient.doOpen();
                    walleClientSet.add(walleClient);
                }


            }
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
        appState=AppState.INITED;
        return true;
    }

    public void  getInterFace(String appName){
        try {
            getWalleRegistry().getData(WalleRegistry.ZK_SPLIT+appName);
        } catch (Exception e) {
          log.error("",e);
        }
    }


    public Set<WalleClient> getWalleClientSet() {
        return walleClientSet;
    }

    public void setWalleClientSet(Set<WalleClient> walleClientSet) {
        this.walleClientSet = walleClientSet;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public WalleRegistry getWalleRegistry() {
        return walleRegistry;
    }

    public void setWalleRegistry(WalleRegistry walleRegistry) {
        this.walleRegistry = walleRegistry;
    }
}
