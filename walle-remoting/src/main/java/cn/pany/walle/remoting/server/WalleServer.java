package cn.pany.walle.remoting.server;


import cn.pany.walle.common.enums.ServerState;
import cn.pany.walle.common.annotation.WalleRpcService;
import cn.pany.walle.common.model.InterfaceDetail;
import cn.pany.walle.common.utils.ExecutorUtil;
import cn.pany.walle.remoting.codec.WalleMessageDecoder;
import cn.pany.walle.remoting.codec.WalleMessageEncoder;
import cn.pany.walle.remoting.registry.WalleRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午4:37:42
 */
public class WalleServer {
    private static final Logger log = LoggerFactory.getLogger(WalleServer.class);

    //    @Value("${registry.address}")
    private String serverAddress = null;
    private int port;

//    private ServerState serverState = ServerState.CREATE_JUST;

    private WalleRegistry walleRegistry;



    //限流--信号量和计数器
    public WalleServer(int port, WalleRegistry walleRegistry) throws UnknownHostException {
        this.port = port;
        this.walleRegistry = walleRegistry;
        serverAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
    }



    protected ChannelFuture future;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    protected void bind() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
//                .option(ChannelOption.SO_BACKLOG, 100)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new WalleMessageDecoder(1024 * 1024, 4, 4))
                                .addLast("MessageEncoder", new WalleMessageEncoder())
                                .addLast("ReadTimeoutHandler", new ReadTimeoutHandler(50))
//                        ch.pipeline().addLast(new LoginAuthRespHandler());
                                .addLast("HeartBeatHandler", new HeartBeatRespHandler())
                                .addLast("WalleServerHandler", new WalleServerHandler());
                    }
                });

        //绑定端口，同步等待成功
        try {
            future = b.bind(this.port).sync();
//            log.info("server started on port {}", this.port);
            log.info("wally server start ok: {}", serverAddress );

//            log.info("wally server start ok:" + (serverAddress));

            future.channel().closeFuture().sync();
        } catch (Exception e) {
//            e.printStackTrace();
            log.error(ThrowableUtil.stackTraceToString(e));
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("workerGroup and bossGroup shutdownGracefully");
        }
    }

    public void shutdown(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public WalleRegistry getWalleRegistry() {
        return walleRegistry;
    }

    public void setWalleRegistry(WalleRegistry walleRegistry) {
        this.walleRegistry = walleRegistry;
    }


}
