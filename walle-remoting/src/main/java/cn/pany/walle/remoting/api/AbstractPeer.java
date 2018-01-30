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

/**
 * AbstractPeer
 *
 * @author pany.young
 * @author william.liangf
 */
public abstract class AbstractPeer implements Endpoint, ChannelHandler {

//    private final ChannelHandler handler;

    private volatile URL url;

    // closing closed分别表示关闭流程中、完成关闭
    private volatile boolean closing;

    private volatile boolean closed;

    public AbstractPeer(URL url) {
            if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
//        if (handler == null) {
//            throw new IllegalArgumentException("handler == null");
//        }
//        this.handler = handler;
    }

    public void send(Object message) throws RemotingException {
        send(message, true);
    }

    public void close() {
        closed = true;
    }

    public void close(int timeout) {
        close();
    }

    public void startClose() {
        if (isClosed()) {
            return;
        }
        closing = true;
    }

    public URL getUrl() {
        return url;
    }

    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
    }

//    public ChannelHandler getChannelHandler() {
//
//            return handler;
//    }

    /**
     * 返回最终的handler，可能已被wrap,需要区别于getChannelHandler
     *
     * @return ChannelHandler
     */
//    public ChannelHandler getDelegateHandler() {
//        return handler;
//    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isClosing() {
        return closing && !closed;
    }


}