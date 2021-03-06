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
