package io.github.lucaseasedup.logit.util;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;

public final class Validators
{
    private Validators()
    {
    }
    
    public static boolean validateEmail(String email)
    {
        if (StringUtils.isBlank(email))
            return false;
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean validateIp(String ip)
    {
        if (StringUtils.isBlank(ip))
            return false;
        
        return InetAddressUtils.isIPv4Address(ip) || InetAddressUtils.isIPv6Address(ip);
    }
    
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    );
}
