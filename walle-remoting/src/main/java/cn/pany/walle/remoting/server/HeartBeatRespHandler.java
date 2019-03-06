package cn.pany.walle.remoting.server;


import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午4:07:41
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {
    private final static Logger log = LoggerFactory.getLogger(HeartBeatRespHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        WalleMessage message = (WalleMessage) msg;
        // 返回心跳应答消息
        if (message.getHeader() != null
                && message.getHeader().getType() == MessageType.HEARTBEAT_REQ) {
            if (log.isDebugEnabled()) {
                log.debug("Receive client heart beat message : ---> "
                        + message);
            }

            WalleMessage heartBeat = buildHeatBeat();
            if (log.isDebugEnabled()) {
                log.debug("Send heart beat response message to client : ---> "
                        + heartBeat);
            }
            ctx.writeAndFlush(heartBeat);
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private WalleMessage buildHeatBeat() {
        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP);
        message.setHeader(header);
        return message;
    }
}
