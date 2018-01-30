package cn.pany.walle.config.spring;

import cn.pany.walle.common.URL;
import cn.pany.walle.remoting.client.WalleClient;
import org.aopalliance.intercept.Invocation;

import java.util.Set;

/**
 * Created by pany on 17/12/11.
 */
public class WalleInvoker<T> implements Invoker<T> {


    private final WalleClient[] clients;

    private final Class<T> type;

    private final URL url;

    public WalleInvoker(Class<T> serviceType, URL url, WalleClient[] clients) {
        this.type = serviceType;
        this.url = url;
        this.clients = clients;
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

}
