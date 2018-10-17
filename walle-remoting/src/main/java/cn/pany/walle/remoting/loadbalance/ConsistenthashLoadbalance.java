package cn.pany.walle.remoting.loadbalance;

import cn.pany.walle.remoting.client.WalleClient;

import java.util.List;
import java.util.Map;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 18/10/16
 */
public class ConsistenthashLoadbalance extends AbstractLoadBalance {

    private static ConsistenthashLoadbalance consistenthashLoadbalance=null;

    public static ConsistenthashLoadbalance getConsistenthashLoadbalance(){
        if(consistenthashLoadbalance==null){
            consistenthashLoadbalance=new ConsistenthashLoadbalance();
        }
        return consistenthashLoadbalance;
    }

    public static String REQUEST_ID = "";

    @Override
    public WalleClient selectorMain( List<WalleClient> clients,Map<String,Object> map) {
//        if(map==null|| map.isEmpty()||map.get(REQUEST_ID)==null){
//            return clients.get(0);
//        }
        int identityHashCode = System.identityHashCode(System.currentTimeMillis());
        int index =  identityHashCode % clients.size();
        return  clients.get(index);
    }
}
