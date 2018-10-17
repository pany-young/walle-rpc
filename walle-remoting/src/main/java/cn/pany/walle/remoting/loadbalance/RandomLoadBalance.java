package cn.pany.walle.remoting.loadbalance;

import cn.pany.walle.remoting.client.WalleClient;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 18/10/16
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    private static RandomLoadBalance randomLoadBalance=null;

    public static RandomLoadBalance getRandomLoadBalance(){
        if(randomLoadBalance==null){
            randomLoadBalance=new RandomLoadBalance();
        }
        return RandomLoadBalance.randomLoadBalance;
    }

    @Override
    public WalleClient selectorMain(List<WalleClient> clients,Map<String, Object> map) {
        int random = (int) ((new Date().getTime()) % clients.size());
        return clients.get(random);
    }
}
