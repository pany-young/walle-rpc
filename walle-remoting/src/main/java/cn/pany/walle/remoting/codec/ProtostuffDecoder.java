/*
 * Copyright 2018-2019 Pany Young.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
