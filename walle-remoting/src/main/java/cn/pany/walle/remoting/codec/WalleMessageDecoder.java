package cn.pany.walle.remoting.codec;

import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleMessage;
import cn.pany.walle.remoting.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午2:24:33
 */
public class WalleMessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger log = LoggerFactory.getLogger(WalleMessageDecoder.class);

    //	MarshallingDecoder marshallingDecoder ;
    ProtostuffDecoder protostuffDecoder;

    public WalleMessageDecoder(int maxFrameLength, int lengthFieldOffset,
                               int lengthFieldLength) throws IOException {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
//		this.marshallingDecoder = new MarshallingDecoder();
        this.protostuffDecoder = new ProtostuffDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
            throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) return null;

        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionID(frame.readLong());
        header.setType(MessageType.getByValue(frame.readByte()));
        header.setPriority(frame.readByte());
        try {
            int size = frame.readInt();

            if (size > 0) {
                Map<String, String> attch = new HashMap<String, String>(size);
                int keySize = 0;
                byte[] keyArray = null;
                String key = null;
                for (int i = 0; i < size; i++) {
                    keySize = frame.readInt();
                    keyArray = new byte[keySize];
                    frame.readBytes(keyArray);
                    key = new String(keyArray, "UTF-8");
//				attch.put(key, marshallingDecoder.decode(frame));

//                    frame.markReaderIndex();
//                    int dataLength = frame.readInt();
//                    if (dataLength < 0) {
//                        ctx.close();
//                    }
//                    if (frame.readableBytes() < dataLength) {
//                        frame.resetReaderIndex();
//                        return message;
//                    }
//                    byte[] data = new byte[dataLength];
//                    frame.readBytes(data);

//                    String value = SerializationUtil.deserialize(data, String.class);
                    String value = (String) protostuffDecoder.decode(frame, String.class);
                    attch.put(key, value);
                }
                keyArray = null;
                key = null;
                header.setAttachment(attch);
            }

            if (frame.readableBytes() > 4) {
//		    message.setBody(marshallingDecoder.decode(frame));
                String bodyClass = (String) header.getAttachment().get("bodyClass");
                Class<?> classtype = Class.forName(bodyClass);//获得Clss对象

                Object messageBody = protostuffDecoder.decode(frame, classtype);

                message.setBody(messageBody);
            }
        } catch (Exception e) {
            log.info("receive cannot decode message:" + ByteBufUtil.hexDump(in));
            log.error("decode error,header:" + header.toString());
            log.error("", e);
            throw e;
        }finally {
            if (!frame.release()) {
                log.error("bytebuffer release false!");
            }
        }
        message.setHeader(header);

        return message;
    }

}
