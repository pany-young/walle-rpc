package cn.pany.walle.remoting.api;

import cn.pany.walle.common.enums.RouterType;
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

    private final Class<T> type;
    private RouterType routerType = RouterType.RANDOM_LOADBALANCE;
    //class#method:version
    private final String invokerUrl;
    public static Map<String, WalleInvoker> walleInvokerMap = new ConcurrentHashMap<>();

    public WalleInvoker(Class<T> serviceType, String invokerUrl) {
        this.type = serviceType;
        this.invokerUrl = invokerUrl;
        walleInvokerMap.putIfAbsent(invokerUrl, this);
    }

    public WalleBizResponse send(WalleMessage walleMessage) {
//                RpcInvocation inv = (RpcInvocation) invocation;
//        final String methodName = RpcUtils.getMethodName(invocation);

        if (walleMessage.getBody() instanceof WalleBizRequest) {
            WalleBizRequest walleBizRequest = (WalleBizRequest) walleMessage.getBody();

            final String methodName = walleBizRequest.getMethodName();

//            Map<String,Object> map =new HashMap();
//            map.put(ConsistenthashLoadbalance.REQUEST_ID,walleBizRequest.getRequestId());

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
//            int index =0;
//            switch (routerType) {
//                case RANDOM_LOADBALANCE:
//                    currentClient = RandomLoadBalance.getRandomLoadBalance().selector(clients,null);
//                    break;
//                case ROUNDROBIN_LOADBALANCE:
//                    currentClient = RoundrobinLoadBalance.getRoundrobinLoadBalance().selector(clients,null);
//                    break;
//                case LEASTACTIVE_LOADBALANCE :
//                    break;
//                case CONSISTENTHASH_LOADBALANCE :
//                    Map<String,Object> map =new HashMap();
//                    map.put(ConsistenthashLoadbalance.REQUEST_ID,requestId);
//                    currentClient = ConsistenthashLoadbalance.getConsistenthashLoadbalance().selector(clients,map);
//                    break;
//            }

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

    public String getInvokerUrl() {
        return invokerUrl;
    }
}
