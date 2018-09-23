package cn.pany.walle.demo.server.boot;

import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Frank
 * Administrator on
 * Date: 14-3-24.
 * Time: ${time}
 * mail : ludongxian@tisson.cn
 */
public final class ServerBootstrap extends AbstractIdleService {

    private ServerBootstrap() {
    }

    private final static Logger LOG = LoggerFactory.getLogger(ServerBootstrap.class);

    private ClassPathXmlApplicationContext context;

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.startAsync();
        try {
            Object lock = new Object();
            synchronized (lock) {
                while (true) {
                    lock.wait();
                }
            }
        } catch (InterruptedException ex) {

            LOG.error("ignore interruption  ! {}",ex);
        }
    }

    /**
     * Start the service.
     */
    @Override
    protected void startUp() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[]{"spring/server-spring-context.xml"});
        context.start();
        context.registerShutdownHook();
        LOG.info("demo service started successfully");
    }

    /**
     * Stop the service.
     */
    @Override
    protected void shutDown() throws Exception {
        context.stop();
        LOG.info("service stopped successfully");
    }
}
