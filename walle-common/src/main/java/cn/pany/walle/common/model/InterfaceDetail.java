package cn.pany.walle.common.model;

/**
 * Created by pany on 18/9/23.
 */
public class InterfaceDetail {
    //class:version
    private String className;
    private String version;
    public InterfaceDetail(String className,String version){
        this.className = className;
        this.version = version;
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
}
