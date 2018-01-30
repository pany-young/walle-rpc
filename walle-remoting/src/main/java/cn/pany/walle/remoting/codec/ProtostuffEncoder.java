package cn.pany.walle.remoting.codec;

import cn.pany.walle.remoting.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;

/**
 * Created by pany on 16/9/5.
 */
public class ProtostuffEncoder {

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];


    protected void encode(Object msg, ByteBuf out) throws Exception {
        try {
            byte[] data = SerializationUtil.serialize(msg);
            int lengthPos = out.writerIndex();
            out.writeBytes(LENGTH_PLACEHOLDER);
//			out.writeInt(data.length);
            out.writeBytes(data);
            out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
