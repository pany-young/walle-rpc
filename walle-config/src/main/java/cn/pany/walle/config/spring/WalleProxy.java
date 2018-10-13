package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.NettyConstant;
import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.api.WalleInvoker;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

/**
 * Created by pany on 16/9/8.
 */

public class WalleProxy {

    public static  <T> T create(Map<String, String> map,WalleInvoker walleInvoker) {
        Class<?> interfaceClass;
        ClassLoader clazzLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            interfaceClass = Class.forName(map.get(NettyConstant.INTERFACE_CLASS_KEY), false, clazzLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
//        URL url= URL.valueOf(map.get("xxx"));

//        WalleProtocol walleProtocol=new WalleProtocol();
//        walleProtocol.refer(interfaceClass,url);

        T t=(T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        WalleMessage nettyMessage = new WalleMessage(); // 创建并初始化 RPC 请求

                        Header header = new Header();
                        header.setType(MessageType.SERVICE_REQ);
                        nettyMessage.setHeader(header);
                        WalleBizRequest walleBizRequest = new WalleBizRequest();
                        walleBizRequest.setRequestId(UUID.randomUUID().toString());
                        walleBizRequest.setClassName(method.getDeclaringClass().getName());
                        walleBizRequest.setMethodName(method.getName());
                        walleBizRequest.setParameterTypes(method.getParameterTypes());
                        walleBizRequest.setParameters(args);
                        nettyMessage.setBody(walleBizRequest);

                        WalleBizResponse response =  walleInvoker.send(nettyMessage); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
//                        WalleBizResponse response = walleClient.send(nettyMessage); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
                        if(response == null){
                            return null;
                        }
                        if (!response.getSuccess()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }

                }
        );
        return t;
    }
}
