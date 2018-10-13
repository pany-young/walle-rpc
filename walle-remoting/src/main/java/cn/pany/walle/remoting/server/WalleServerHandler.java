package cn.pany.walle.remoting.server;

import cn.pany.walle.common.protocol.MessageType;
import cn.pany.walle.remoting.protocol.Header;
import cn.pany.walle.remoting.protocol.WalleBizRequest;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import cn.pany.walle.remoting.protocol.WalleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.pany.walle.common.protocol.MessageType.*;

/**
 * Created by pany on 17/11/5.
 */
public class WalleServerHandler extends SimpleChannelInboundHandler<WalleMessage>{
    private final static Logger LOG = LoggerFactory.getLogger(WalleServerHandler.class);


    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 300, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(50));

    public static Map<String, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WalleMessage msg) throws Exception {
        processMessageReceived(ctx, msg);
    }

    public void processMessageReceived(ChannelHandlerContext ctx, WalleMessage msg) throws Exception {
        final WalleMessage cmd = msg;
        if (cmd != null) {
            switch (cmd.getHeader().getType()) {
                case SERVICE_REQ:
//                    processRequestCommand(ctx, cmd);
                    LOG.info("biz message:"+msg.toString());
//                    LOG.info("biz message:"+msg.getBody().toString());
                    threadPoolExecutor.execute(new WalleBizTask(ctx,cmd));
                    break;
                case SERVICE_RESP:
//                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    public class WalleBizTask implements  Runnable{
        ChannelHandlerContext channelHandlerContext;
        WalleMessage message;
        WalleBizTask(ChannelHandlerContext channelHandlerContext,WalleMessage message ){
            this.channelHandlerContext=channelHandlerContext;
            this.message=message;
        }

        private Object handle(WalleBizRequest request) throws Exception {

            if (request == null)
                return null;
            String className = request.getClassName();
            Object serviceBean = handlerMap.get(className);

            if(serviceBean!=null){
                Class<?> serviceClass = serviceBean.getClass();
                String methodName = request.getMethodName();
                Class<?>[] parameterTypes = request.getParameterTypes();
                Object[] parameters = request.getParameters();

                FastClass serviceFastClass = FastClass.create(serviceClass);
                FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
                Object result = serviceFastMethod.invoke(serviceBean, parameters);
                return result;
            }else {
                return null;
            }
        }

        @Override
        public void run() {
            WalleMessage messageResponse = buildBizResp();
            WalleBizResponse response = new WalleBizResponse();
            try {
                WalleBizRequest request = (WalleBizRequest) message.getBody();

                response.setRequestId(request.getRequestId());

                Object result = this.handle(request);

                response.setResult(result);
                response.setSuccess(true);
            } catch (Exception t) {
                response.setError(t);
            }
            messageResponse.setBody(response);
            channelHandlerContext.writeAndFlush(messageResponse);
        }
    }

    private WalleMessage buildBizResp() {
        WalleMessage message = new WalleMessage();
        Header header = new Header();
        header.setType(MessageType.SERVICE_RESP);
        message.setHeader(header);
        return message;
    }

    public static void putHandlerMapBean(String interfaceName, Object serviceBean) throws BeansException {
        handlerMap.put(interfaceName, serviceBean);
    }
}
