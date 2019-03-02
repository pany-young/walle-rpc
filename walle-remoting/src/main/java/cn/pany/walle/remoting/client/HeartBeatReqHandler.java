package cn.pany.walle.remoting.client;


import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.common.utils.ExecutorUtil;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午4:07:41
 */
@Slf4j
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HeartBeatReqHandler.class);

    private WalleClient walleClient;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    //    private volatile ScheduledFuture<?> heartBeat;
    private volatile HeartBeatTask heartBeatTask;

//    private long HEART_BEAT_TIME =20000;
    private long HEART_BEAT_TIME =1000;

    public HeartBeatReqHandler(WalleClient walleClient) {
        super();
        this.walleClient = walleClient;

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildHeatBeat());
        heartBeatTask = new HeartBeatReqHandler.HeartBeatTask(ctx);
        ctx.executor().schedule(heartBeatTask,
                HEART_BEAT_TIME, TimeUnit.MILLISECONDS);
//        heartBeat = ctx.executor().scheduleWithFixedDelay(new HeartBeatReqHandler.HeartBeatTask(ctx),
//                0, 20000, TimeUnit.MILLISECONDS);
//        heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx),
//                0, 20000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        WalleMessage message = (WalleMessage) msg;
        // 返回心跳应答消息
        if (message.getHeader() != null
                && message.getHeader().getType() == MessageType.HEARTBEAT_RESP) {
            log.debug("Receive client heart beat message : ---> "
                    + message);
//            WalleMessage heartBeat = buildHeatBeat();
            log.debug("Send heart beat response message to client : ---> "
                    + heartBeatTask.toString());
//            ctx.writeAndFlush(heartBeat);
            ctx.executor().schedule(heartBeatTask,
                    HEART_BEAT_TIME, TimeUnit.MILLISECONDS);
        } else {
            ctx.fireChannelRead(msg);

        }
    }

    private WalleMessage buildHeatBeat() {
        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_REQ);
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        if(heartBeat!=null){
//            heartBeat.cancel(true);
//            heartBeat=null;
//        }
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getAddress().getHostAddress();
        log.info("HeartBeatReqHandler 断开:" + ip + ":" + address.getPort());
        //清理invoker里的链接
        walleClient.close();
    }

    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            WalleMessage heatBeat = buildHeatBeat();
//			System.out.println("Client send heart beat message to server :-->"+heatBeat);
            ctx.writeAndFlush(heatBeat);
        }

    }
}
