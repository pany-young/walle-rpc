package cn.pany.walle.remoting.client;

import cn.pany.walle.common.URL;
import cn.pany.walle.common.constants.NettyConstant;
import cn.pany.walle.common.utils.ExecutorUtil;
import cn.pany.walle.common.utils.NamedThreadFactory;
import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.remoting.api.WalleChannel;
import cn.pany.walle.remoting.exception.RemotingException;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pany on 17/12/13.
 */
@Slf4j
public abstract class AbstractClient implements WalleChannel<WalleMessage,WalleBizResponse>{

    private volatile URL url;

    // closing closed分别表示关闭流程中、完成关闭
    private volatile boolean closing;

    private volatile boolean closed;

    private static final AtomicInteger CLIENT_THREAD_POOL_ID = new AtomicInteger();
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("DubboClientReconnectTimer", true));
    private final Lock connectLock = new ReentrantLock();
    private final AtomicInteger reconnect_count = new AtomicInteger(0);

    protected volatile ExecutorService executor;
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;

    public AbstractClient(URL url) throws RemotingException {

       // shutdown_timeout = url.getParameter(Constants.SHUTDOWN_TIMEOUT_KEY, Constants.DEFAULT_SHUTDOWN_TIMEOUT);
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

        executor =  new ThreadPoolExecutor(200, 250, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(50));
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
        return (InetSocketAddress) channel.localAddress();
    }

//    public void send(Object message, boolean sent) throws RemotingException {
//        if (!isConnected()) {
//            connect();
//        }
//        boolean success = true;
//        int timeout = 0;
//        Channel channel = getChannel();
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
//
//        if (!success) {
//            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress()
//                    + "in timeout(" + timeout + "ms) limit");
//        }
//    }


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
        try {
            if (executor != null) {
                ExecutorUtil.shutdownNow(executor, 100);
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
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

    public void close(int timeout) {
        ExecutorUtil.gracefulShutdown(executor, timeout);
        close();
    }

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
    public void startClose() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }


    public abstract WalleBizResponse send(WalleMessage request) throws RemotingException;
}
