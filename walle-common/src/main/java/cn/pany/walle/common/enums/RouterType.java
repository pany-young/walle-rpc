package cn.pany.walle.common.enums;

import cn.pany.walle.common.protocol.MessageType;

/**
 * Created by pany on 18/10/15.
 */
public enum RouterType {
    RANDOM_LOADBALANCE(1,"随机"),
    ROUNDROBIN_LOADBALANCE(2,"轮询"),
    LEASTACTIVE_LOADBALANCE(3,"最少调用数"),
    CONSISTENTHASH_LOADBALANCE(4,"一致性 Hash");

    private int code;
    private String name;

    private RouterType(int code,String name){
        this.code=code;
        this.name=name;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public static RouterType getByValue(int code){
        for(RouterType routerType:RouterType.values()){
            if(routerType.code==code){
                return routerType;
            }
        }
        return null;
    }
}
