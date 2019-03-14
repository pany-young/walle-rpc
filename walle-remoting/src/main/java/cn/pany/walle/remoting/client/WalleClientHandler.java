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
package cn.pany.walle.remoting.client;

import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by pany on 17/11/5.
 */
public class WalleClientHandler extends SimpleChannelInboundHandler<WalleMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(WalleClientHandler.class);

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
                    LOG.info("biz message:" + msg.toString());

                    break;
                case SERVICE_RESP:
                    if (cmd.getBody() != null) {
                        WalleBizResponse walleBizResponse = (WalleBizResponse) cmd.getBody();
                        WalleClient.received(walleBizResponse.getRequestId(), walleBizResponse);
//                    processResponseCommand(ctx, cmd);
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private WalleMessage buildBizResp() {
        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setType(MessageType.SERVICE_RESP);
        message.setHeader(header);
        return message;
    }

}
