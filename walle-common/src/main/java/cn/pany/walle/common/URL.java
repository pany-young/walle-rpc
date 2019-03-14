
package cn.pany.walle.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class URL implements Serializable {

    private final static Logger log = LoggerFactory.getLogger(URL.class);

    private final String protocol;

    private final String host;
    private String version;
    private final int port;

    private final Map<String, String> parameters;


    protected URL() {
        this.protocol = null;
        this.host = null;
        this.port = 0;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, host, port, null, (Map<String, String>) null);
    }


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
        String version = null;
        String host = null;
        int port = 0;
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



    public int getPort() {
        return port;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }



    public Map<String, String> getParameters() {
        return parameters;
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
