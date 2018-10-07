package cn.pany.walle.common.constants;

import java.util.regex.Pattern;

/**
 * Created by pany on 17/12/26.
 */
public class Constants {

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern
            .compile("\\s*[|;]+\\s*");

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");

}
