/*
 * Copyright 1999-2011 Alibaba Group.
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
package cn.pany.walle.common.utils;


import cn.pany.walle.common.URL;

import java.util.*;

public class UrlUtils {

    //ip:port#version@protocol
    public static URL parseURL(String address, Map<String, String> defaults) {
        if (address == null || address.length() == 0) {
            return null;
        }
        String url = null;
        if (address.indexOf(":") >= 0) {
            url = address;
        }

        String defaultProtocol = defaults == null ? null : defaults.get("protocol");
        if (defaultProtocol == null || defaultProtocol.length() == 0) {
            defaultProtocol = "protocol";
        }
//        String defaultUsername = defaults == null ? null : defaults.get("username");
//        String defaultPassword = defaults == null ? null : defaults.get("password");
        int defaultPort = StringUtils.parseInteger(defaults == null ? null : defaults.get("port"));
//        String defaultPath = defaults == null ? null : defaults.get("path");
        Map<String, String> defaultParameters = defaults == null ? null : new HashMap<String, String>(defaults);
        if (defaultParameters != null) {
            defaultParameters.remove("protocol");
//            defaultParameters.remove("username");
//            defaultParameters.remove("password");
            defaultParameters.remove("host");
            defaultParameters.remove("port");
//            defaultParameters.remove("path");
        }
        URL u = URL.valueOf(url);
        boolean changed = false;
        String protocol = u.getProtocol();
//        String username = u.getUsername();
//        String password = u.getPassword();
        String version = u.getVersion();
        String host = u.getHost();
        int port = u.getPort();
//        String path = u.getPath();
        Map<String, String> parameters = new HashMap<String, String>(u.getParameters());
        if ((protocol == null || protocol.length() == 0) && defaultProtocol != null && defaultProtocol.length() > 0) {
            changed = true;
            protocol = defaultProtocol;
        }
//        if ((username == null || username.length() == 0) && defaultUsername != null && defaultUsername.length() > 0) {
//            changed = true;
//            username = defaultUsername;
//        }
//        if ((password == null || password.length() == 0) && defaultPassword != null && defaultPassword.length() > 0) {
//            changed = true;
//            password = defaultPassword;
//        }
        if (port <= 0) {
            if (defaultPort > 0) {
                changed = true;
                port = defaultPort;
            } else {
                changed = true;
                port = 9988;
            }
        }
//        if (path == null || path.length() == 0) {
//            if (defaultPath != null && defaultPath.length() > 0) {
//                changed = true;
//                path = defaultPath;
//            }
//        }
        if (defaultParameters != null && defaultParameters.size() > 0) {
            for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
                String key = entry.getKey();
                String defaultValue = entry.getValue();
                if (defaultValue != null && defaultValue.length() > 0) {
                    String value = parameters.get(key);
                    if (value == null || value.length() == 0) {
                        changed = true;
                        parameters.put(key, defaultValue);
                    }
                }
            }
        }
        if (changed) {
            u = new URL(protocol, host, port, version,parameters);
        }
        return u;
    }


}