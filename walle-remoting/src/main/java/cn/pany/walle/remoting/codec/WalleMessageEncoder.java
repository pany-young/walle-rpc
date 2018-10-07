package cn.pany.walle.remoting.codec;

import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午1:49:37
 */
@Slf4j
public class WalleMessageEncoder extends MessageToByteEncoder<WalleMessage> {
    //	private MarshallingEncoder marshallingEncoder;
    private ProtostuffEncoder protostuffEncoder;
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    public WalleMessageEncoder() throws IOException {
//		marshallingEncoder = new MarshallingEncoder();
        protostuffEncoder = new ProtostuffEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, WalleMessage msg,
                          ByteBuf out) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new Exception("The encode message is null");
        }
        if (msg.getBody() != null) {
            String msgBodyClass = msg.getBody().getClass().getName();
            msg.getHeader().getAttachment().put("bodyClass", msgBodyClass);
        }

        out.writeInt(msg.getHeader().getCrcCode());
        out.writeInt(msg.getHeader().getLength());
        out.writeLong(msg.getHeader().getSessionID());
        out.writeByte(msg.getHeader().getType().value());
        out.writeByte(msg.getHeader().getPriority());
        out.writeInt((msg.getHeader().getAttachment().size()));
        String key = null;
        byte[] keyArray = null;
        Object value = null;
        for (Map.Entry<String, String> param : msg.getHeader().getAttachment().entrySet()) {
            key = param.getKey();
            keyArray = key.getBytes("UTF-8");
            out.writeInt(keyArray.length);
            out.writeBytes(keyArray);
            value = param.getValue();
//			marshallingEncoder.encode(value, out);

            //用protostuff序列化
            protostuffEncoder.encode(value,out);
        }
        key = null;
        keyArray = null;
        value = null;
        if (msg.getBody() != null) {
//			marshallingEncoder.encode(msg.getBody(), out);

            //用protostuff序列化
            protostuffEncoder.encode(msg.getBody(),out);
        } else {
            out.writeInt(0);
        }
        out.setInt(4, out.readableBytes() - 8);

    }


}
