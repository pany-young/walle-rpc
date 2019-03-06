package cn.pany.walle.common.model;

import cn.pany.walle.common.utils.InvokerUtil;

/**
 * Created by pany on 18/9/23.
 */
public class InterfaceDetail {
    //class:version
    private String className;
    private String version;
    private String interfaceUrl;
    public InterfaceDetail(String className,String version){
        this.className = className;
        this.version = version;
        interfaceUrl = InvokerUtil.formatInvokerUrl(className,null,version);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }
}
