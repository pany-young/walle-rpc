//package cn.pany.walle.config.spring;

//import cn.pany.walle.common.utils.ConcurrentHashSet;
//import cn.pany.walle.remoting.api.WalleChannelHandler;
//import cn.pany.walle.remoting.api.WalleChannel;
//import cn.pany.walle.remoting.client.WalleClient;
//import cn.pany.walle.remoting.exception.RemotingException;
//import cn.pany.walle.remoting.exception.RpcException;
//
//import java.util.Set;
//
///**
// * Created by pany on 17/12/11.
// */
//public class WalleProtocol {
//
//    protected final Set<Invoker<?>> invokers = new ConcurrentHashSet<Invoker<?>>();
//
//    private WalleChannelHandler requestHandler=new WalleChannelHandler() {
//        @Override
//        public void connected(WalleChannel walleChannel) throws RemotingException {
//
//        }
//
//        @Override
//        public void disconnected(WalleChannel walleChannel) throws RemotingException {
//
//        }
//
//        @Override
//        public void sent(WalleChannel walleChannel, Object message) throws RemotingException {
//
//        }
//
//        @Override
//        public void received(WalleChannel walleChannel, Object message) throws RemotingException {
//
//        }
//
//        @Override
//        public void caught(WalleChannel walleChannel, Throwable exception) throws RemotingException {
//
//        }
//    };
//
//
//}
