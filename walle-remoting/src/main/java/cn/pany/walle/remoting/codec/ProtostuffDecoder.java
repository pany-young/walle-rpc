package cn.pany.walle.remoting.codec;

import cn.pany.walle.remoting.exception.NettyCodeException;
import cn.pany.walle.remoting.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;

/**
 * Created by pany on 16/9/5.
 */
public class ProtostuffDecoder {

    protected Object decode(ByteBuf in, Class<?> classtype) throws Exception {
        in.markReaderIndex();
        int objectSize = in.readInt();
        if (objectSize < 0) {
            throw new NettyCodeException("objectSize is 0");
        }
        if (in.readableBytes() < objectSize) {
            throw new NettyCodeException("readableBytes less than objectSize");
        }

        byte[] data = new byte[objectSize];
        in.readBytes(data);

        Object obj = SerializationUtil.deserialize(data, classtype);

        return obj;

    }

}
