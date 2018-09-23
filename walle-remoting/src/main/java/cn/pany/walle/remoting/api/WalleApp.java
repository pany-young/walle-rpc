package cn.pany.walle.remoting.api;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.registry.WalleRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * Created by pany on 18/1/22.
 */
@Slf4j
public class WalleApp {
    private String appName;

    private Set<WalleClient> walleClientSet;

    private WalleRegistry walleRegistry;
    private  AppState appState=AppState.INIT;
    enum AppState{
        INIT(1),INITED(2),CLOSE(9);

        private int code;
        AppState(int code){this.code=code;}
    }

    public WalleApp(String appName, WalleRegistry walleRegistry) {
        this.walleRegistry = walleRegistry;
        this.appName = appName;
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
            List<String> serverList = walleRegistry.getData(appName);

            //ip:port#version@protocol
            for (String serverDetail : serverList) {
                URL url = UrlUtils.parseURL(serverDetail, null);
                List<String> interfaceList =walleRegistry.getData(appName + "/" + serverDetail);
                WalleClient walleClient = new WalleClient(this,url,interfaceList);
                walleClient.doOpen();

                walleClientSet.add(walleClient);
            }
        } catch (Exception e) {
            log.error("", e);
            return false;
        }

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
