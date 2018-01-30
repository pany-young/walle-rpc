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


import cn.pany.walle.remoting.exception.RemotingException;

public interface ChannelHandler {

    /**
     * on walleChannel connected.
     *
     * @param walleChannel walleChannel.
     */
    void connected(WalleChannel walleChannel) throws RemotingException;

    /**
     * on walleChannel disconnected.
     *
     * @param walleChannel walleChannel.
     */
    void disconnected(WalleChannel walleChannel) throws RemotingException;

    /**
     * on message sent.
     *
     * @param walleChannel walleChannel.
     * @param message message.
     */
    void sent(WalleChannel walleChannel, Object message) throws RemotingException;

    /**
     * on message received.
     *
     * @param walleChannel walleChannel.
     * @param message message.
     */
    void received(WalleChannel walleChannel, Object message) throws RemotingException;

    /**
     * on exception caught.
     *
     * @param walleChannel   walleChannel.
     * @param exception exception.
     */
    void caught(WalleChannel walleChannel, Throwable exception) throws RemotingException;

}