package cn.pany.walle.remoting.client;

import cn.pany.walle.common.*;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.remoting.api.ChannelHandler;
import cn.pany.walle.remoting.api.NettyChannelHandler;
import cn.pany.walle.remoting.codec.WalleMessageDecoder;
import cn.pany.walle.remoting.codec.WalleMessageEncoder;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.protocol.SessionObj;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
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

    private Map<String, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系

    public static final ConcurrentHashMap<String, Object> pushFutureMap = new ConcurrentHashMap();

    public static final ConcurrentHashMap<String, WalleBizResponse> responseMap = new ConcurrentHashMap();

    private Bootstrap bootstrap ;
    private URL url;
    private volatile Channel channel; // volatile, please copy reference to use

    public static List<SessionObj> sessionObjList = new ArrayList<SessionObj>();

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    public WalleClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url);
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
            bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new WalleMessageDecoder(1024 * 1024, 4, 4))
                                    .addLast("MessageEncoder", new WalleMessageEncoder())
                                    .addLast("readTimeoutHandler", new ReadTimeoutHandler(100));
//                                    .addLast("LoginAuthHandler", new LoginAuthReqHandler())
//                                    .addLast("HeartBeatHandler", new HeartBeatReqHandler())
//                                    .addLast("BizReqHandler", new BizReqHandler());
                        }
                    })
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
                            NettyChannelHandler.removeChannelIfDisconnected(oldChannel);
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
                            NettyChannelHandler.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        WalleClient.this.channel = newChannel;
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
    protected void doDisConnect() throws Throwable {

    }



    public WalleBizResponse send(WalleMessage request) throws Exception {
        long random = (new Date().getTime()) % sessionObjList.size();
        SessionObj sessionObj = sessionObjList.get((int) random);
        ChannelFuture channelFuture = sessionObj.getChannel().writeAndFlush(request);

        String requestId = ((WalleBizRequest) request.getBody()).getRequestId();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        pushFutureMap.putIfAbsent(requestId, countDownLatch);

        countDownLatch.await(10, TimeUnit.SECONDS);

        WalleBizResponse response = responseMap.get(requestId);
        responseMap.remove(requestId);
        return response;
    }


    public static void received(String requestId, WalleBizResponse walleBizResponse) {

        responseMap.putIfAbsent(requestId, walleBizResponse);

        CountDownLatch countDownLatch = (CountDownLatch) pushFutureMap.get(requestId);

        countDownLatch.countDown();
        pushFutureMap.remove(requestId);
    }



    @Override
    public void send(Object message) throws RemotingException {

    }


}
