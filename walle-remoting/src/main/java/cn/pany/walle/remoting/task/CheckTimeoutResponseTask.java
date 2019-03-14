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
package cn.pany.walle.remoting.task;

import cn.pany.walle.remoting.client.WalleClient;
import cn.pany.walle.remoting.protocol.WalleBizResponse;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class CheckTimeoutResponseTask implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(WalleClient.class);

    @Override
    public void run() {
        log.debug("run CheckTimeoutResponseTask");
        Date now= new Date();
//        Long nowTime =System.currentTimeMillis();
        for(Map.Entry<String,WalleBizResponse> entry  :  WalleClient.responseMap.entrySet()){
            WalleBizResponse response =  entry.getValue();
            Date timeoutTime = DateUtils.addSeconds(response.getReceiveTime(),response.getTimeOutNum().intValue());
            if(now.compareTo(timeoutTime) >0){
                WalleClient.responseMap.remove(entry.getKey());
            }

        }

    }

}
