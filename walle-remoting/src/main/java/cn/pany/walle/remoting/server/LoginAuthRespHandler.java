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
package cn.pany.walle.remoting.server;


import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.LoginBody;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pany
 * @version 1.0
 * @createDate：2015年12月16日 下午3:27:04
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private final static Logger log = LoggerFactory.getLogger(LoginAuthRespHandler.class);

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();

//    private String[] whiteList = {"127.0.0.1", "192.168.3.8", "192.168.96.173"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        WalleMessage message = (WalleMessage) msg;
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_REQ) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            WalleMessage loginResp = null;
            //重复登录，拒绝
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResp = buildResponse((byte) -1);
            } else {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = true;
//                for (String WIP : whiteList) {
//                    if (WIP.equals(ip)) {
//                        isOK = true;
//                    }
//                }
                loginResp = isOK ? buildResponse((byte) 0) : buildResponse((byte) -1);
//				loginResp=buildResponse((byte)0);
                if (isOK) nodeCheck.put(ip, true);

                log.info("The login response is :" + loginResp + " body[" + loginResp.getBody() + "]");
                ctx.writeAndFlush(loginResp);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private WalleMessage buildResponse(byte b) {
        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP);
        message.setHeader(header);

//		message.setBody(b);

        LoginBody loginBody = new LoginBody();
        loginBody.setIsOk(b);
        loginBody.setPort(1);
        loginBody.setIp("123");
        message.setBody(loginBody);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }


}
