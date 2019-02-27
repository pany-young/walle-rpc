package cn.pany.walle.remoting.client;

import cn.pany.walle.common.URL;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pany on 17/12/13.
 */
public abstract class AbstractClient implements WalleChannel<WalleMessage,WalleBizResponse>{
    private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);
    private volatile URL url;
    private final Lock connectLock = new ReentrantLock();
    private final AtomicInteger reconnect_count = new AtomicInteger(0);

//    protected volatile ExecutorService executor;
//    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;

    public AbstractClient(URL url) throws RemotingException {

        this.url = url;
        try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new RemotingException(url.toInetSocketAddress(), null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        try {
            connect();
            if (log.isInfoEnabled()) {
                log.info("Start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + getRemoteAddress());
            }
        } catch (RemotingException t) {
                close();
                throw t;

        } catch (Throwable t) {
            close();
            throw new RemotingException(url.toInetSocketAddress(), null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }

//        executor =  new ThreadPoolExecutor(200, 250, 0, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>(50));
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
                        + NetUtils.getLocalHost()  + ", cause: Connect wait timeout: " + "ms.");
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Successed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                            + NetUtils.getLocalHost() +   ", channel is " + this.getChannel());
                }
            }
            afterConnect();
            reconnect_count.set(0);
        } catch (RemotingException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalHost()   + ", cause: " + e.getMessage(), e);
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

    public void reconnect() throws RemotingException {
        disconnect();
        connect();
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
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

//    public void close(int timeout) {
//        ExecutorUtil.gracefulShutdown(executor, timeout);
//        close();
//    }

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
