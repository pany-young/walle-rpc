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
package cn.pany.walle.common.model;

import cn.pany.walle.common.utils.NetUtils;
import cn.pany.walle.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


public final class InvokerUrl implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    private final static Logger log = LoggerFactory.getLogger(InvokerUrl.class);


    private String method;

//    private final String username;

//    private final String password;

    private String interfaceName;
    private String version;


    protected InvokerUrl() {

    }

    public InvokerUrl(String interfaceName, String method, String version) {
        this.interfaceName = interfaceName;
        this.method = method;
        this.version = version;
    }


    /**
     * Parse url string
     *
     * @param url URL string
     * @return URL instance
     * @see InvokerUrl
     * //ip:port#version@protocol
     */
    public static InvokerUrl valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String interfaceName = null;
        String version = null;
        String method = null;
        int i=0;



        i = url.indexOf(":");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing host: \"" + url + "\"");
            version = url.substring(i+1 ,url.length());
            url = url.substring(0, i);

        }

        i = url.indexOf("@");
        if (i >= 0) {
            method = url.substring(i +1, url.length());
            interfaceName = url.substring(0, i);

        }


        return new InvokerUrl(interfaceName, method, version);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
