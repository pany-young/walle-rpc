package cn.pany.walle.remoting.loadbalance;

import cn.pany.walle.common.utils.AtomicPositiveInteger;
import cn.pany.walle.remoting.client.WalleClient;

import java.util.List;
import java.util.Map;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 18/10/16
 */
public class RoundrobinLoadBalance extends AbstractLoadBalance {

    private static RoundrobinLoadBalance roundrobinLoadBalance=null;

    public static RoundrobinLoadBalance getRoundrobinLoadBalance(){
        if(roundrobinLoadBalance==null){
            roundrobinLoadBalance=new RoundrobinLoadBalance();
        }
        return roundrobinLoadBalance;
    }

    AtomicPositiveInteger atomicPositiveInteger =new AtomicPositiveInteger();


    @Override
    public WalleClient selectorMain( List<WalleClient> clients,Map<String,Object> map) {
        int index =  (atomicPositiveInteger.incrementAndGet()) % clients.size();
        return clients.get(index);

    }
}
