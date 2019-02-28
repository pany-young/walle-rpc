package cn.pany.walle.remoting.client;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.constants.WalleConstant;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.utils.InvokerUtil;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.remoting.api.Invoker;
import cn.pany.walle.remoting.api.NettyWalleChannelHandler;
import cn.pany.walle.remoting.api.WalleApp;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.codec.WalleMessageDecoder;
import cn.pany.walle.remoting.codec.WalleMessageEncoder;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.protocol.SessionObj;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import cn.pany.walle.remoting.registry.WalleRegistry;
import cn.pany.walle.remoting.task.CheckTimeoutResponseTask;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by pany on 16/9/6.
 */

public class WalleClient extends AbstractClient {
    private static final Logger log = LoggerFactory.getLogger(WalleClient.class);

    public static final ConcurrentHashMap<String, CountDownLatch> pushFutureMap = new ConcurrentHashMap();

    public static final ConcurrentHashMap<String, WalleBizResponse> responseMap = new ConcurrentHashMap();

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    private Bootstrap bootstrap ;
//    private URL url;
    private volatile Channel channel; // volatile, please copy reference to use

    public SessionObj sessionObj = new SessionObj();

    private WalleApp walleApp;
    private List<InterfaceDetail> interfaceList;
    private Map<String,WalleClient> interfaceMap=new HashMap<>();
    private WalleRegistry walleRegistry;

    static {
        Long timeout= WalleConstant.DEFAULT_TIMEOUT;
        log.info("CheckTimeoutResponseTask start");
        scheduledExecutorService.scheduleWithFixedDelay(new CheckTimeoutResponseTask(),0,timeout,TimeUnit.MILLISECONDS);
    }


    public WalleClient(WalleApp walleApp,URL url,List<InterfaceDetail> interfaceList, WalleRegistry walleRegistry) throws RemotingException {
        super(url);
        this.walleApp=walleApp;
        this.interfaceList = interfaceList;
        this.walleRegistry=walleRegistry;
        sessionObj.setRemoteIP(url.getHost());
        sessionObj.setPort(url.getPort());
        sessionObj.setChannel(channel);
        for(InterfaceDetail interfaceDetail : interfaceList){
            String interfaceUrl = InvokerUtil.formatInvokerUrl(interfaceDetail.getClassName(),null,interfaceDetail.getVersion());
            interfaceMap.put(interfaceUrl,this);
        }
    }


    @Override
    public void doOpen() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ChannelHandler handler = new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast("WalleMessageDecoder",new WalleMessageDecoder(1024 * 1024, 4, 4))
                            .addLast("WallesMessageEncoder", new WalleMessageEncoder())
                            .addLast("ReadTimeoutHandler", new ReadTimeoutHandler(50))
//                                    .addLast("LoginAuthHandler", new LoginAuthReqHandler())
                                    .addLast("HeartBeatHandler", new HeartBeatReqHandler(WalleClient.this))
                                    .addLast("WalleClientHandler", new WalleClientHandler());
                }
            };
            bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(handler)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            log.info("WalleClient doOpen success!address is:[{}]",getUrl().getAddress());
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
        ChannelFuture future = bootstrap.connect(getUrl().getHost(), getUrl().getPort());
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
                    }
                }
            } else if (future.cause() != null) {
                throw new RemotingException(this, "walle client failed to connect to server "
                        + getRemoteAddress() + ", error message is:" + future.cause().getMessage(), future.cause());
            } else {
                throw new RemotingException(this, "walle client failed to connect to server "
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
        walleApp.getWalleClientSet().add(this);
        for (InterfaceDetail interfaceDetail : interfaceList) {
            String invokerUrl = InvokerUtil.formatInvokerUrl(interfaceDetail.getClassName(), null, interfaceDetail.getVersion());

            WalleInvoker walleInvoker = WalleInvoker.walleInvokerMap.get(invokerUrl);
            if (walleInvoker == null) {
                walleInvoker = new WalleInvoker<>(interfaceDetail.getClass(), invokerUrl);
            }

            if (!walleInvoker.getClients().contains(this)) {
                walleInvoker.addToClients(this);
            }
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        log.info("doDisConnect:"+getUrl().getAddress());
        for( InterfaceDetail interfaceDetail : interfaceList){
            WalleInvoker walleInvoker =  WalleInvoker.walleInvokerMap.get(interfaceDetail.getInterfaceUrl());
            if(walleInvoker!=null){
                walleInvoker.getClients().remove(this);
            }
        }
    }

    @Override
    protected Channel getChannel() {
        return this.channel;
    }



    @Override
    public WalleBizResponse send(WalleMessage request) throws RemotingException {
        if (!isConnected()) {
            connect();
        }

        //这里强转可能会失败
        if(request.getBody() instanceof WalleBizRequest ){
            String requestId = ((WalleBizRequest) request.getBody()).getRequestId();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            pushFutureMap.putIfAbsent(requestId, countDownLatch);

            try {
                getChannel().writeAndFlush(request);
//            if(countDownLatch.await(WalleConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)){
                Long timeout= WalleConstant.DEFAULT_TIMEOUT;
                if(countDownLatch.await(timeout, TimeUnit.MILLISECONDS)){
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
                log.error("requestId [{"+requestId+"}] is time out",e);
                return null;
            }catch (Exception e){
                log.error("requestId [{"+requestId+"}] is error",e);
                return null;
            }
        }

        return null;

    }


    public static void received(String requestId, WalleBizResponse walleBizResponse) {
        CountDownLatch countDownLatch = pushFutureMap.get(requestId);
        if(countDownLatch!=null){
            countDownLatch.countDown();
            pushFutureMap.remove(requestId);
            walleBizResponse.setReceiveTime(new Date());
            walleBizResponse.setTimeOutNum(WalleConstant.DEFAULT_TIMEOUT);
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

    public List<InterfaceDetail> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(List<InterfaceDetail> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public Map<String, WalleClient> getInterfaceMap() {
        return interfaceMap;
    }

    public void setInterfaceMap(Map<String, WalleClient> interfaceMap) {
        this.interfaceMap = interfaceMap;
    }

    public void setChannel(Channel channel) {
        this.channel=channel;
        if(sessionObj!=null){
            sessionObj.setChannel(channel);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;
        if(o instanceof WalleClient){
            WalleClient that = (WalleClient) o;
            if(getUrl() == null || that.getUrl()==null ||that.getUrl().getAddress()==null||getUrl().getAddress()==null){
                return false;
            }
            log.info("this addressis :"+getUrl().getAddress());
            log.info("that addressis :"+that.getUrl().getAddress());

            return getUrl().getAddress().equals(that.getUrl().getAddress());
        }

        return false;

    }

    @Override
    public int hashCode() {
        return getUrl().getAddress().hashCode();
    }


}
