package com.sls.wguide.wguide;

/**
 * Created by сергей on 12.05.2015.
 */
public class AccessPoint {



    public AccessPoint (String SSID, String BSSID, int LEVEL, String Encrypt, long TIME)
    {
        setSSID(SSID);
        setBSSID(BSSID);
        setLevel(LEVEL);
        setEncrypt(Encrypt);
        setTime(TIME);
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    private boolean check;


    public String getWho_add() {
        return who_add;
    }

    public void setWho_add(String who_add) {
        this.who_add = who_add;
    }

    private String who_add;


    public AccessPoint ()
    {
        super();
    }

    private String BSSID;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    private int ID;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    private String SSID;
    private int level;

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private double lat;
    private double lon;
    private String encrypt;
    private long time;

}
