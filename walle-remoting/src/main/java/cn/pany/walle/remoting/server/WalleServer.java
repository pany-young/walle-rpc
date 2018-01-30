package cn.pany.walle.remoting.server;


import cn.pany.walle.common.ServerState;
import cn.pany.walle.common.annotation.WalleService;
import cn.pany.walle.remoting.codec.WalleMessageDecoder;
import cn.pany.walle.remoting.codec.WalleMessageEncoder;
import cn.pany.walle.remoting.registry.WalleRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午4:37:42
 */
@Slf4j
public class WalleServer implements ApplicationContextAware, InitializingBean {


    //    @Value("${registry.address}")
    private String serverAddress = null;
    private int port;

    private ServerState serverState = ServerState.CREATE_JUST;

    private WalleRegistry walleRegistry;

    private Map<String, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系

    //限流--信号量和计数器
    public WalleServer(int port, int threadNum){
        this.port=port;
    }

    //Spring时，扫描写入
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(WalleService.class); // 获取所有带有 WalleService 注解的 Spring Bean
        if (!serviceBeanMap.isEmpty()) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(WalleService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }


    //非Spring时，直接写入
    public void putHandlerMap(String interfaceName,Object serviceBean) throws BeansException {
        handlerMap.put(interfaceName, serviceBean);
    }

    private void bind() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new WalleMessageDecoder(1024 * 1024, 4, 4));
                        ch.pipeline().addLast("MessageEncoder", new WalleMessageEncoder());
                        ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
//                        ch.pipeline().addLast(new LoginAuthRespHandler());
                        ch.pipeline().addLast("HeartBeatHandler", new HeartBeatRespHandler());
//                      ch.pipeline().addLast("BizRespHandler", new BizRespHandler(handlerMap));
                    }

                });

        //绑定端口，同步等待成功
        try {
            ChannelFuture future = b.bind(this.port).sync();
            log.info("server started on port {}", this.port);


            if (walleRegistry != null) {
                serverAddress=InetAddress.getLocalHost().getHostAddress() + ":" + port;
                walleRegistry.register(serverAddress); // 注册服务地址
            }
            log.info("wally server start ok:" + (serverAddress));

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getStackTrace().toString());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void afterPropertiesSet() throws Exception {

        this.bind();
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
