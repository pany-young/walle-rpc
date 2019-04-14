package cn.pany.walle.common.model;

import java.util.List;
import java.util.Set;

/**
 * Created by pany on 18/9/23.
 */
public class ServerInfo {
    Set<InterfaceDetail> interfaceDetailSet;

    public Set<InterfaceDetail> getInterfaceDetailSet() {
        return interfaceDetailSet;
    }

    public void setInterfaceDetailSet(Set<InterfaceDetail> interfaceDetailSet) {
        this.interfaceDetailSet = interfaceDetailSet;
    }
}
