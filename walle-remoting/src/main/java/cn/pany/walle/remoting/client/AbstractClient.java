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

import cn.pany.walle.common.URL;
import cn.pany.walle.common.utils.NamedThreadFactory;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.common.utils.StringUtils;
import cn.pany.walle.remoting.api.WalleChannel;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pany on 17/12/13.
 */
public abstract class AbstractClient implements WalleChannel<WalleMessage, WalleBizResponse> {
    private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);
    private volatile URL url;
    private final Lock connectLock = new ReentrantLock();
    private final AtomicInteger reconnectCount = new AtomicInteger(0);//重连次数
    private long reconnectDelayTimes;//重连延迟时间

    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("WalleClientReconnectTimer", true));
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;

    public AbstractClient(URL url) throws RemotingException {
        this.url = url;
    }

    public void init() throws RemotingException {

        try {
            doOpen();
            connect();
            if (log.isInfoEnabled()) {
                log.info("Start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + getRemoteAddress());
            }
        } catch (RemotingException t) {
            close();
            throw t;

        } catch (Throwable t) {
            log.error("", t);
            close();
            throw new RemotingException(url.toInetSocketAddress(), null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
    }


    protected void connect() throws RemotingException {
        connectLock.lock();
        try {
            if (isConnected()) {
                return;
            }

            doConnect();
            if (!isConnected()) {
                throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                        + NetUtils.getLocalHost() + ", cause: Connect wait timeout: " + "ms.");
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Successed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                            + NetUtils.getLocalHost() + ", channel is " + this.getChannel());
                }
            }
            afterConnect();
            reconnectCount.set(0);
        } catch (RemotingException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalHost() + ", cause: " + e.getMessage(), e);
        } finally {
            connectLock.unlock();
        }
    }

    /**
     * Open client.
     *
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Close client.
     *
     * @throws Throwable
     */
    protected abstract void doClose() throws Throwable;

    /**
     * Connect to server.
     *
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    protected abstract void afterConnect() throws Throwable;

    /**
     * disConnect to server.
     *
     * @throws Throwable
     */
    protected abstract void doDisConnect() throws Throwable;

    /**
     * Get the connected channel.
     *
     * @return channel
     */
    protected abstract Channel getChannel();

    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return getUrl().toInetSocketAddress();
        return (InetSocketAddress) channel.localAddress();
    }

    public void disconnect() {
        connectLock.lock();
        try {
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }

    public void reConnect() {
        close();
        //  异步检查zookpeer是否还有这个服务器，如果有，则重连（重连失败则定时再重连）。如无，则停止。
        if (checkIfNeedReconnect()) {
            try {
                reconnectCount.incrementAndGet();
                connect();
            } catch (RemotingException re) {
                log.error("", re);
                if(reconnectCount.get()>100){
                    log.error("reconnectCount large than 100!");
                }else {
                    if (reconnectExecutorFuture == null || reconnectExecutorFuture.isDone()) {
                        reconnectDelayTimes += 20;//每次重连加20秒延迟
                        // 放入重连线程池
                        reconnectExecutorFuture = reconnectExecutorService.schedule(() -> reConnect(), reconnectDelayTimes, TimeUnit.SECONDS);
                    }
                }
            }
        }
    }

    public void close() {
//        try {
//            if (executor != null) {
//                ExecutorUtil.shutdownNow(executor, 100);
//            }
//        } catch (Throwable e) {
//            log.warn(e.getMessage(), e);
//        }
        try {
            disconnect();
            doClose();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }

    }


    public abstract boolean checkIfNeedReconnect();

    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        if (channel == null)
            return getUrl().toInetSocketAddress();
        return (InetSocketAddress) channel.remoteAddress();
    }

    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null)
            return false;
        return channel.isActive();
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isClosed() {
        return false;
    }


    public abstract WalleBizResponse send(WalleMessage request) throws RemotingException;
}
