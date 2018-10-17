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
package cn.pany.walle.common;

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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL - Uniform Resource Locator (Immutable, ThreadSafe)
 * <p/>
 * url example:
 * <ul>
 * <li>http://www.facebook.com/friends?param1=value1&amp;param2=value2
 * <li>http://username:password@10.20.130.230:8080/list?version=1.0.0
 * <li>ftp://username:password@192.168.1.7:21/1/read.txt
 * <li>registry://192.168.1.7:9090/com.alibaba.service1?param1=value1&amp;param2=value2
 * </ul>
 * <p/>
 * Some strange example below:
 * <ul>
 * <li>192.168.1.3:20880<br>
 * for this case, url protocol = null, url host = 192.168.1.3, port = 20880, url path = null
 * <li>file:///home/user1/router.js?type=script<br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>file://home/user1/router.js?type=script<br>
 * for this case, url protocol = file, url host = home, url path = user1/router.js
 * <li>file:///D:/1/router.js?type=script<br>
 * for this case, url protocol = file, url host = null, url path = D:/1/router.js
 * <li>file:/D:/1/router.js?type=script<br>
 * same as above file:///D:/1/router.js?type=script
 * <li>/home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = home, url path = user1/router.js
 * </ul>
 *
 * @author william.liangf
 * @author ding.lid
 * @see java.net.URL
 * @see java.net.URI
 */
public final class URL implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    private final static Logger log = LoggerFactory.getLogger(URL.class);


    private final String protocol;

//    private final String username;

//    private final String password;

    private final String host;
    private String version;

    private final int port;

//    private final String path;

    private final Map<String, String> parameters;

    // ==== cache ====

    private volatile transient Map<String, Number> numbers;

    private volatile transient Map<String, URL> urls;

    private volatile transient String ip;

    private volatile transient String parameter;

    protected URL() {
        this.protocol = null;
//        this.username = null;
//        this.password = null;
        this.host = null;
        this.port = 0;
//        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, host, port, null, (Map<String, String>) null);
    }

//    public URL(String protocol, String host, int port, Map<String, String> parameters) {
//        this(protocol,  host, port,  parameters);
//    }

//    public URL(String protocol, String host, int port, String path) {
//        this(protocol,  host, port, path, (Map<String, String>) null);
//    }


    public URL(String protocol, String host, int port, String version, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        // trim the beginning "/"
        this.version = version;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }


//    public URL(String protocol, String username, String password, String host, int port, String path) {
//        this(protocol, username, password, host, port, path, (Map<String, String>) null);
//    }


//    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
//        if ((username == null || username.length() == 0)
//                && password != null && password.length() > 0) {
//            throw new IllegalArgumentException("Invalid url, password without username!");
//        }
//        this.protocol = protocol;
//        this.username = username;
//        this.password = password;
//        this.host = host;
//        this.port = (port < 0 ? 0 : port);
//        // trim the beginning "/"
//        while (path != null && path.startsWith("/")) {
//            path = path.substring(1);
//        }
//        this.path = path;
//        if (parameters == null) {
//            parameters = new HashMap<String, String>();
//        } else {
//            parameters = new HashMap<String, String>(parameters);
//        }
//        this.parameters = Collections.unmodifiableMap(parameters);
//    }

    /**
     * Parse url string
     *
     * @param url URL string
     * @return URL instance
     * @see URL
     * //ip:port#version@protocol
     */
    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
//        String username = null;
//        String password = null;
        String version = null;
        String host = null;
        int port = 0;
//        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?"); // seperator between body and parameters 
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }

        i = url.indexOf("@");
        if (i >= 0) {
            protocol = url.substring(0, i);
            url = url.substring(i + 1);
        }

        i = url.indexOf(":");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing host: \"" + url + "\"");
            host = url.substring(0, i);
            url = url.substring(i + 1);
        }
//        else {
//            // case: file:/path/to/file.txt
//            i = url.indexOf(":/");
//            if (i >= 0) {
//                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
//                protocol = url.substring(0, i);
//                url = url.substring(i + 1);
//            }
//        }

        i = url.indexOf("#");
        if (i >= 0 && i < url.length() - 1) {
            version = url.substring(i + 1);
            url = url.substring(0, i);
        }

        if (url.length() > 0) {
            try {
                port = Integer.parseInt(url);
            } catch (Exception e) {
                log.error("",e);
            }
        }
        return new URL(protocol, host, port, version, parameters);
    }

    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getProtocol() {
        return protocol;
    }


    public String getHost() {
        return host;
    }

    /**
     * 获取IP地址.
     * <p/>
     * 请注意：
     * 如果和Socket的地址对比，
     * 或用地址作为Map的Key查找，
     * 请使用IP而不是Host，
     * 否则配置域名会有问题
     *
     * @return ip
     */
    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }


//    private String appendDefaultPort(String address, int defaultPort) {
//        if (address != null && address.length() > 0
//                && defaultPort > 0) {
//            int i = address.indexOf(':');
//            if (i < 0) {
//                return address + ":" + defaultPort;
//            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
//                return address.substring(0, i + 1) + defaultPort;
//            }
//        }
//        return address;
//    }

    public Map<String, String> getParameters() {
        return parameters;
    }


    private Map<String, Number> getNumbers() {
        if (numbers == null) { // 允许并发重复创建
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }

    private Map<String, URL> getUrls() {
        if (urls == null) { // 允许并发重复创建
            urls = new ConcurrentHashMap<String, URL>();
        }
        return urls;
    }

    public URL addParameterAndEncoded(String key, String value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, encode(value));
    }

    public URL addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Enum<?> value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Number value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        // 如果没有修改，直接返回。
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, host, port, version, map);
    }


    /**
     * Add parameters to a new url.
     *
     * @param parameters
     * @return A new URL
     */
    public URL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null) {
                if (entry.getValue() != null) {
                    hasAndEqual = false;
                    break;
                }
            } else {
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual) return this;

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new URL(protocol, host, port, version, map);
    }


    public URL addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }

    public URL addParameterString(String query) {
        if (query == null || query.length() == 0) {
            return this;
        }
        return addParameters(StringUtils.parseQueryString(query));
    }

    public URL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    public URL removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    public URL removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new URL(protocol, host, port, version, map);
    }

    public URL clearParameters() {
        return new URL(protocol, host, port, version, new HashMap<String, String>());
    }


    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }


    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        URL other = (URL) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (port != other.port)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        return true;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
