package cn.pany.walle.config.spring;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.utils.ConcurrentHashSet;
import cn.pany.walle.remoting.api.ChannelHandler;
import cn.pany.walle.remoting.api.WalleChannel;
import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.exception.RpcException;

import java.util.Set;

/**
 * Created by pany on 17/12/11.
 */
public class WalleProtocol {

    protected final Set<Invoker<?>> invokers = new ConcurrentHashSet<Invoker<?>>();

    private ChannelHandler requestHandler=new ChannelHandler() {
        @Override
        public void connected(WalleChannel walleChannel) throws RemotingException {

        }

        @Override
        public void disconnected(WalleChannel walleChannel) throws RemotingException {

        }

        @Override
        public void sent(WalleChannel walleChannel, Object message) throws RemotingException {

        }

        @Override
        public void received(WalleChannel walleChannel, Object message) throws RemotingException {

        }

        @Override
        public void caught(WalleChannel walleChannel, Throwable exception) throws RemotingException {

        }
    };

    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        // create rpc invoker.
        WalleInvoker<T> invoker = new WalleInvoker<T>(serviceType, url, getClients(url));
        invokers.add(invoker);
        return invoker;
    }


    private WalleClient[] getClients(URL url)  {


        WalleClient[] clients = new WalleClient[1];
        for (int i = 0; i < clients.length; i++) {

                clients[i] = initClient(url);

        }
        return clients;
    }

    private WalleClient initClient(URL url)   {

        WalleClient client = null;
        try {
            client = new WalleClient(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), e);
        }

        return client;
    }
}
