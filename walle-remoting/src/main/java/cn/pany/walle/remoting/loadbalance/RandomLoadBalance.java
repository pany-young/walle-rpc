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
