package cn.pany.walle.common.utils;

/**
 * @author pany young
 * @email dev_pany@163.com
 * @date 2019/1/25
 */
public class InvokerUtil {
    public static String formatInvokerUrl(String className, String method, String version) {

        if(className==null){
            throw new RuntimeException("className is not allow null!");
        }

        String returnStr = className + (method == null ? "" : "#" + method);
        returnStr = returnStr + (version == null ? "" : ":" + version);
        return returnStr;
    }
}
