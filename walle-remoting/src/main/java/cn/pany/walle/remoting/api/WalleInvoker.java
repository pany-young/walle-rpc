package cn.pany.walle.remoting.api;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.enums.RouterType;
import cn.pany.walle.common.model.InvokerUrl;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.exception.RpcException;
import cn.pany.walle.remoting.loadbalance.AbstractLoadBalance;
import cn.pany.walle.remoting.loadbalance.ConsistenthashLoadbalance;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pany on 17/12/11.
 */
public class WalleInvoker<T> implements Invoker<T> {


    private List<WalleClient> clients = new ArrayList<>();
    private final InvokerUrl invokerUrl;
    private final Class<T> type;
    private RouterType routerType = RouterType.RANDOM_LOADBALANCE;
    //class#method:version
    private final String invokerUrlStr;
    public static Map<String, WalleInvoker> walleInvokerMap = new ConcurrentHashMap<>();

    private final String version;
    public WalleInvoker(Class<T> serviceType, String invokerUrlStr ) {
        this.type = serviceType;
        this.invokerUrlStr = invokerUrlStr;
        this.invokerUrl = InvokerUrl.valueOf(invokerUrlStr);
        this.version =invokerUrl.getVersion();

        walleInvokerMap.putIfAbsent(invokerUrlStr, this);
    }

    public WalleInvoker(Class<T> serviceType, String invokerUrlStr,RouterType routerType ) {
        this.type = serviceType;
        this.invokerUrlStr = invokerUrlStr;
        this.invokerUrl = InvokerUrl.valueOf(invokerUrlStr);
        this.routerType =routerType;
        this.version=invokerUrl.getVersion();
        walleInvokerMap.putIfAbsent(invokerUrlStr, this);
    }

    public WalleBizResponse send(WalleMessage walleMessage) {
        if (walleMessage.getBody() instanceof WalleBizRequest) {
//            WalleBizRequest walleBizRequest = (WalleBizRequest) walleMessage.getBody();
//            final String methodName = walleBizRequest.getMethodName();

            WalleClient currentClient = selectorClient(null);

            try {
                return currentClient.send(walleMessage);
            } catch (RemotingException e) {
//                throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
                throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + ", cause: ", e);
            }
        } else {
            return null;
        }
    }

    private WalleClient selectorClient(Map map) {
        if (clients.isEmpty()) {
            return null;
        }
        WalleClient currentClient = null;
        if (clients.size() == 1) {
            currentClient = clients.get(0);
        } else {
            if (AbstractLoadBalance.getLoadBanlance(routerType) != null) {
                AbstractLoadBalance.getLoadBanlance(routerType).selector(clients, map);
            }
        }

        return currentClient;
    }

    public void addToClients(WalleClient client) {
        clients.add(client);
    }

    public List<WalleClient> getClients() {
        return clients;
    }

    public void setClients(List<WalleClient> clients) {
        this.clients = clients;
    }

    public Class<T> getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public InvokerUrl getInvokerUrl() {
        return invokerUrl;
    }


    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public String getInvokerUrlStr() {
        return invokerUrlStr;
    }
}
