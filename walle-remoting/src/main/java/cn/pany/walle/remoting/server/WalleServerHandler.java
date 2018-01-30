package cn.pany.walle.remoting.server;

import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static cn.pany.walle.common.protocol.MessageType.*;

/**
 * Created by pany on 17/11/5.
 */
public class WalleServerHandler extends SimpleChannelInboundHandler<WalleMessage>

{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WalleMessage msg) throws Exception {
        processMessageReceived(ctx, msg);
    }

    public void processMessageReceived(ChannelHandlerContext ctx, WalleMessage msg) throws Exception {
        final WalleMessage cmd = msg;
        if (cmd != null) {
            switch (cmd.getHeader().getType()) {
                case SERVICE_REQ:
//                    processRequestCommand(ctx, cmd);
                    break;
                case SERVICE_RESP:
//                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }


}
