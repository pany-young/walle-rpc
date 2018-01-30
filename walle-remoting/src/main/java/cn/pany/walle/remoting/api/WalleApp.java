package cn.pany.walle.remoting.api;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.utils.UrlUtils;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.registry.WalleRegistry;

import java.util.List;
import java.util.Set;

/**
 * Created by pany on 18/1/22.
 */
public class WalleApp {
    private String appName;

    private Set<WalleClient> walleClientSet;

    private WalleRegistry walleRegistry;


    public WalleApp(String appName,WalleRegistry walleRegistry){
        this.walleRegistry=walleRegistry;
        this.appName =appName;
    }

    public void init() throws Exception {
        if(walleRegistry.isRegister()){
            walleRegistry.register();
        }
       List<String> appList = walleRegistry.getData(appName);

        //ip:port#version@protocol
        for(String appDetail : appList){

            URL url = UrlUtils.parseURL(appDetail,null);
            WalleClient walleClient=new WalleClient(url,);


            walleClientSet.add(walleClient);
        }


    }



}
