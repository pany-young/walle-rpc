package cn.pany.walle.remoting.protocol;

import java.io.Serializable;

/**
 * Created by pany on 16/3/3.
 */
public class LoginBody implements Serializable{

    private byte isOk;//消息类型

    private int port;//消息类型
    private String ip;//消息类型

    public byte getIsOk() {
        return isOk;
    }

    public void setIsOk(byte isOk) {
        this.isOk = isOk;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
