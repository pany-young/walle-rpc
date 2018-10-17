package cn.pany.walle.remoting.loadbalance;

import cn.pany.walle.remoting.client.WalleClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 18/10/16
 */
public interface LoadBalance {

    public WalleClient selector( List<WalleClient> clients,Map<String,Object> map);

}
