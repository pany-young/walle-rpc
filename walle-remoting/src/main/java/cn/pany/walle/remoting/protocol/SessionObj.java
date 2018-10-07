package cn.pany.walle.remoting.protocol;

import io.netty.channel.Channel;

/**
 * Created by pany on 16/3/2.
 */
public class SessionObj {

    private String remoteIP;//IP

    private int port;//端口

    private volatile Channel channel;

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
