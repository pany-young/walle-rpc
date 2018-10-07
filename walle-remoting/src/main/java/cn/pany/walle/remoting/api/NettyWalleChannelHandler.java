/*
 * Copyright 1999-2011 Alibaba Group.
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
package cn.pany.walle.remoting.api;


import cn.pany.walle.common.URL;
import cn.pany.walle.remoting.exception.RemotingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import io.netty.channel.Channel;
/**
 * NettyWalleChannelHandler.
 *
 * @author pany.yang
 */
@Slf4j
public final class NettyWalleChannelHandler implements WalleChannelHandler {

    private static final ConcurrentMap<Channel, NettyWalleChannelHandler> channelMap = new ConcurrentHashMap<Channel, NettyWalleChannelHandler>();

    private final Channel channel;

    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private NettyWalleChannelHandler(Channel channel, URL url, WalleChannelHandler handler) {

        if ( channel == null) {
            throw new IllegalArgumentException("netty channel == null;");
        }
        this.channel =  channel;
    }

    public static NettyWalleChannelHandler getOrAddChannel(Channel ch, URL url, WalleChannelHandler handler) {
        if (ch == null) {
            return null;
        }
        NettyWalleChannelHandler ret = channelMap.get(ch);
        if (ret == null) {
            NettyWalleChannelHandler nettyChannel = new NettyWalleChannelHandler(ch, url, handler);
            if (ch.isActive()) {
                ret = channelMap.putIfAbsent(ch, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }

    public static void removeChannelIfDisconnected(Channel ch) {
        if (ch != null && !ch.isActive()) {
            channelMap.remove(ch);
        }
    }

//    public InetSocketAddress getLocalAddress() {
//        return (InetSocketAddress) channel.localAddress();
//    }

//    public InetSocketAddress getRemoteAddress() {
//        return (InetSocketAddress) channel.remoteAddress();
//    }

//    public boolean isConnected() {
//        return channel.isActive();
//    }

//    public void send(Object message, boolean sent) throws RemotingException {
//        boolean success = true;
//        int timeout = 0;
//        try {
//            ChannelFuture future = channel.writeAndFlush(message);
//            if (sent) {
//                timeout = NettyConstant.DEFAULT_TIMEOUT;
//                success = future.await(timeout);
//            }
//            Throwable cause = future.cause();
//            if (cause != null) {
//                throw cause;
//            }
//        } catch (Throwable e) {
//            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
//        }

//        if (!success) {
//            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress()
//                    + "in timeout(" + timeout + "ms) limit");
//        }
//    }

    public void close() {
        try {
            removeChannelIfDisconnected(channel);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        try {
            attributes.clear();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        try {
            if (log.isInfoEnabled()) {
                log.info("Close netty channel " + channel);
            }
            channel.close();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        if (value == null) { // The null value unallowed in the ConcurrentHashMap.
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NettyWalleChannelHandler other = (NettyWalleChannelHandler) obj;
        if (channel == null) {
            if (other.channel != null) return false;
        } else if (!channel.equals(other.channel)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Nettychannel [channel=" + channel + "]";
    }

    @Override
    public void connected(WalleChannel walleChannel) throws RemotingException {

    }

    @Override
    public void disconnected(WalleChannel walleChannel) throws RemotingException {

    }

    @Override
    public void sent(WalleChannel walleChannel, Object message) throws RemotingException {

    }

    @Override
    public void received(WalleChannel walleChannel, Object message) throws RemotingException {

    }

    @Override
    public void caught(WalleChannel walleChannel, Throwable exception) throws RemotingException {

    }
}