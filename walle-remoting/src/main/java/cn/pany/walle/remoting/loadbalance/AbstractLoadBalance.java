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
package cn.pany.walle.remoting.loadbalance;

import cn.pany.walle.common.enums.RouterType;
import cn.pany.walle.remoting.client.WalleClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 18/10/16
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    public static Map<RouterType,LoadBalance> loadBalanceMap=new HashMap();

    static {
        loadBalanceMap.put(RouterType.RANDOM_LOADBALANCE,RandomLoadBalance.getRandomLoadBalance() );
        loadBalanceMap.put(RouterType.ROUNDROBIN_LOADBALANCE,RoundrobinLoadBalance.getRoundrobinLoadBalance() );
        loadBalanceMap.put(RouterType.CONSISTENTHASH_LOADBALANCE,ConsistenthashLoadbalance.getConsistenthashLoadbalance());
    }

    public static LoadBalance getLoadBanlance(RouterType routerType){
       return loadBalanceMap.get(routerType);
    }

    public WalleClient selector( List<WalleClient> clients,Map<String, Object> map) {
        if (clients == null || clients.isEmpty()) {
            return null;
        }

        return selectorMain(clients,map);
    }

    public abstract WalleClient selectorMain(List<WalleClient> clients,Map<String, Object> map);
}
