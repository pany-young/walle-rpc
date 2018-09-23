package cn.pany.walle.remoting.client;

import cn.pany.walle.common.*;
import cn.pany.walle.common.constants.NettyConstant;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.remoting.api.NettyWalleChannelHandler;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.codec.WalleMessageDecoder;
import cn.pany.walle.remoting.codec.WalleMessageEncoder;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.protocol.SessionObj;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by pany on 16/9/6.
 */
@Slf4j
public class WalleClient extends AbstractClient implements ApplicationContextAware {


    public static final ConcurrentHashMap<String, CountDownLatch> pushFutureMap = new ConcurrentHashMap();

    public static final ConcurrentHashMap<String, WalleBizResponse> responseMap = new ConcurrentHashMap();

    private Bootstrap bootstrap ;
    private URL url;
    private volatile Channel channel; // volatile, please copy reference to use

    public SessionObj sessionObj = new SessionObj();

//    private Map<String, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系
//    public static List<SessionObj> sessionObjList = new ArrayList<SessionObj>();
//    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    private WalleApp walleApp;
    List<String> interfaceList;
    Map<String,WalleClient> interfaceMap=new HashMap<>();

    public WalleClient(WalleApp walleApp,URL url) throws RemotingException {
        super(url);
        this.walleApp=walleApp;
    }
    public WalleClient(WalleApp walleApp,URL url,List<String> interfaceList) throws RemotingException {
        super(url);
        this.walleApp=walleApp;
        this.interfaceList = interfaceList;
        for(String interfaceDetail : interfaceList){
            interfaceMap.put(interfaceDetail,this);
        }
    }

    //    @Resource
//    private ServiceDiscovery serviceDiscovery;


    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {

    }
    @Override
    public void doOpen() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            io.netty.channel.ChannelHandler handler = new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new WalleMessageDecoder(1024 * 1024, 4, 4))
                            .addLast("MessageEncoder", new WalleMessageEncoder())
                            .addLast("readTimeoutHandler", new ReadTimeoutHandler(100));
//                                    .addLast("LoginAuthHandler", new LoginAuthReqHandler())
//                                    .addLast("HeartBeatHandler", new HeartBeatReqHandler())
//                                    .addLast("BizReqHandler", new BizReqHandler());
                }
            };
            bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(handler)
                    .option(ChannelOption.SO_KEEPALIVE, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doClose() throws Throwable {

    }

    @Override
    protected void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(url.getHost(), url.getPort());
        try {
            boolean ret = future.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);

            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    // 关闭旧的连接
                    Channel oldChannel = WalleClient.this.channel; // copy reference
                    if (oldChannel != null) {
                        try {
                            if (log.isInfoEnabled()) {
                                log.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                            }
                            oldChannel.close();
                        } finally {
                            NettyWalleChannelHandler.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (WalleClient.this.isClosed()) {
                        try {
                            if (log.isInfoEnabled()) {
                                log.info("Close new netty channel " + newChannel + ", because the client closed.");
                            }
                            newChannel.close();
                        } finally {
                            WalleClient.this.channel = null;
                            NettyWalleChannelHandler.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        WalleClient.this.channel = newChannel;
                        //获取接口列表 todo


                    }
                }
            } else if (future.cause() != null) {
                throw new RemotingException(this, "client(url: " + getUrl() + ") failed to connect to server "
                        + getRemoteAddress() + ", error message is:" + future.cause().getMessage(), future.cause());
            } else {
                throw new RemotingException(this, "client(url: " + getUrl() + ") failed to connect to server "
                        + getRemoteAddress()  + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client "
                        + NetUtils.getLocalHost() );
            }
        } finally {
            if (!isConnected()) {
                future.cancel(true);
            }
        }
    }

    @Override
    protected void afterConnect() throws Throwable {
        //获取接口信息
//        walleApp.getInterFace();
    }

    @Override
    protected void doDisConnect() throws Throwable {
        //todo 断开连接处理
        //1.是否断开重连，重连次数

    }

    @Override
    protected Channel getChannel() {
        return this.channel;
    }


//    @Override
//    public WalleBizResponse send(WalleMessage request,boolean sent) throws Exception {
//    if (!isConnected()) {
//            connect();
//        }
//        boolean success = true;
//        int timeout = 0;
//        Channel channel = getChannel();
//        try {
//            ChannelFuture future = channel.writeAndFlush(message);
//
//        } catch (Throwable e) {
//            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
//        }
//
//        if (!success) {
//            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress()
//                    + "in timeout(" + timeout + "ms) limit");
//        }
//    }
    @Override
    public WalleBizResponse send(WalleMessage request) throws RemotingException {
        if (!isConnected()) {
            connect();
        }

//        long random = (new Date().getTime()) % sessionObjList.size();
//        SessionObj sessionObj = sessionObjList.get((int) random);
        ChannelFuture channelFuture = sessionObj.getChannel().writeAndFlush(request);

        //todo 这里强转可能会失败
        String requestId = ((WalleBizRequest) request.getBody()).getRequestId();

//        try {
//            channelFuture.await(NettyConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            log.error("requestId [{}] is time out",requestId,e);
//            return null;
//        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        pushFutureMap.putIfAbsent(requestId, countDownLatch);

        try {
            if(countDownLatch.await(NettyConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)){
                WalleBizResponse response = responseMap.get(requestId);
                responseMap.remove(requestId);
                return response;
            }else {
                pushFutureMap.remove(requestId);
                WalleBizResponse response = responseMap.get(requestId);
                responseMap.remove(requestId);
                return response;
            }
        } catch (InterruptedException e) {
            log.error("requestId [{}] is time out",requestId,e);
            return null;
        }
    }


    public static void received(String requestId, WalleBizResponse walleBizResponse) {


        CountDownLatch countDownLatch = pushFutureMap.get(requestId);
        if(countDownLatch!=null){
            countDownLatch.countDown();
            pushFutureMap.remove(requestId);
            responseMap.putIfAbsent(requestId, walleBizResponse);
        }else {
            log.info("requestId :[{}] not in pushFutureMap,rep:[{}]",requestId, JSON.toJSONString(walleBizResponse) );
        }

    }


    public WalleApp getWalleApp() {
        return walleApp;
    }

    public void setWalleApp(WalleApp walleApp) {
        this.walleApp = walleApp;
    }

    public List<String> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(List<String> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public Map<String, WalleClient> getInterfaceMap() {
        return interfaceMap;
    }

    public void setInterfaceMap(Map<String, WalleClient> interfaceMap) {
        this.interfaceMap = interfaceMap;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
