package com.sls.wguide.wguide;


/**
 * Created by Sls on 14.05.2015.
 */
public class myUtil {

    public String SecurTypeWiFi (String pars)
    {
        final String WPA2 = "WPA2";
        final String WPA = "WPA";
        final String WEP = "WEP";

        if (pars.contains(WPA))
            if (pars.contains(WPA2))
                return WPA2;
            else
                return WPA;
        else if (pars.contains(WEP))
            return WEP;
        else
            return "Open";
    }


}
