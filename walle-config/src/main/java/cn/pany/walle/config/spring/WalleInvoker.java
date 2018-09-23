package cn.pany.walle.config.spring;

import cn.pany.walle.common.URL;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.exception.RpcException;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import org.aopalliance.intercept.Invocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by pany on 17/12/11.
 */
public class WalleInvoker<T> implements Invoker<T> {


    private List<WalleClient> clients = new ArrayList<>();

    private final Class<T> type;

    //class#method:version
    private final String invokerUrl;

    public WalleInvoker(Class<T> serviceType, String invokerUrl) {
        this.type = serviceType;
        this.invokerUrl = invokerUrl;
    }

//    @Override
//    protected Result doInvoke(final Invocation invocation) throws Throwable {
//        RpcInvocation inv = (RpcInvocation) invocation;
//        final String methodName = RpcUtils.getMethodName(invocation);
//
//        WalleClient currentClient;
//        if (clients.length == 1) {
//            currentClient = clients[0];
//        } else {
//            currentClient = clients[index.getAndIncrement() % clients.length];
//        }
//        try {
//            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
//            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
//            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
//            if (isOneway) {
//                boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
//                currentClient.send(inv, isSent);
//                RpcContext.getContext().setFuture(null);
//                return new RpcResult();
//            } else if (isAsync) {
//                ResponseFuture future = currentClient.request(inv, timeout);
//                RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
//                return new RpcResult();
//            } else {
//                RpcContext.getContext().setFuture(null);
//                return (Result) currentClient.request(inv, timeout).get();
//            }
//        } catch (TimeoutException e) {
//            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
//        } catch (RemotingException e) {
//            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
//        }
//    }

    public WalleBizResponse send(WalleMessage walleMessage){
//                RpcInvocation inv = (RpcInvocation) invocation;
//        final String methodName = RpcUtils.getMethodName(invocation);

        if(walleMessage.getBody() instanceof WalleBizRequest){
            WalleBizRequest walleBizRequest = (WalleBizRequest)walleMessage.getBody();

            final String methodName = walleBizRequest.getMethodName();

            WalleClient currentClient;
            if (clients.size() == 1) {
                currentClient = clients.get(0);
            } else {
                int random = (int) ((new Date().getTime()) % clients.size());
                currentClient = clients.get(random);
            }
            try {
//                boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
//                boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
//                int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
//                if (isOneway) {
//                    boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
//                    currentClient.send(inv, isSent);
//                    RpcContext.getContext().setFuture(null);
//                    return new RpcResult();
//                } else if (isAsync) {
//                    ResponseFuture future = currentClient.request(inv, timeout);
//                    RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
//                    return new RpcResult();
//                } else {
//                    RpcContext.getContext().setFuture(null);
//                    return (Result) currentClient.request(inv, timeout).get();
//                }
               return currentClient.send(walleMessage);

            }  catch (RemotingException e) {
//                throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
                throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + ", cause: ", e);
            }
        } else {
            return null;
        }



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
