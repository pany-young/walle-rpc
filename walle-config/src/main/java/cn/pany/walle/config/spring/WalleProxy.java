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
package cn.pany.walle.config.spring;

import cn.pany.walle.common.constants.WalleConstant;
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
            interfaceClass = Class.forName(map.get(WalleConstant.INTERFACE_CLASS_KEY), false, clazzLoader);
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
                        WalleMessage walleMessage = new WalleMessage(); // 创建并初始化 RPC 请求

                        Header header = new Header();
                        header.setType(MessageType.SERVICE_REQ);
                        walleMessage.setHeader(header);
                        WalleBizRequest walleBizRequest = new WalleBizRequest();
                        walleBizRequest.setRequestId(UUID.randomUUID().toString());
                        walleBizRequest.setClassName(method.getDeclaringClass().getName());
                        walleBizRequest.setMethodName(method.getName());
                        walleBizRequest.setVersion(walleInvoker.getVersion());
                        walleBizRequest.setParameterTypes(method.getParameterTypes());
                        walleBizRequest.setParameters(args);
                        walleMessage.setBody(walleBizRequest);

                        WalleBizResponse response =  walleInvoker.send(walleMessage); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
//                        WalleBizResponse response = walleClient.send(nettyMessage); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
                        if(response == null){
                            return null;
                        }
                        if (response.getSuccess()==null&&response.getError()!=null) {
                            throw response.getError();
                        }else if(!response.getSuccess()&&response.getError()!=null){
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
