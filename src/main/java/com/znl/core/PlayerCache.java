package com.znl.core;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/18.
 */
public class PlayerCache implements Serializable {
    private String account = "unknown";//玩家账号
    private int areId = 0;//区id
    private String utma = "unknown";//手机唯一标识
    private String imei = "unknown";//IMEI
    private String screen = "unknown";//分辨率
    private int os = 0;//手机系统 1、Android，2、iOS，3、其他
    private String osName = "unknown";//手机系统，具体名称
    private String model = "unknown";//手机机型
    private String net = "unknown";//网络 1、2G,2、3G,3、WiFi，4、其他
    private String operators = "unknown";//运营商 1、移动；2、联通；3、电信；4、其他
    private String location = "unknown";//地理坐标
    private String package_name = "unknown";//游戏包名称
    private String package_size = "unknown";//游戏包大小(字节数)
    private String fill_register_msg_times = "unknown";//平台账号注册填写信息时间(秒)
    private int startup_times = 0;//启动时间(秒)，android核加载flash到游戏启动时间
    private int plat_id = 0;//平台ID
    private String plat_name = "unknown";//所属平台
    private String user_ip = "unknown";//用户IP
    private String game_version = "unknown";//游戏版本
    private String pushChanelId; //推送ID
    private Long playerId;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public PlayerCache() {
    }

    public PlayerCache(String account, int areId, String utma, String imei, String screen, int os, String osName, String model, String net, String operators, String location, String package_name, String package_size, String fill_register_msg_times, int startup_times, int plat_id, String plat_name, String user_ip, String game_version) {
        this.account = account;
        this.areId = areId;
        this.utma = utma;
        this.imei = imei;
        this.screen = screen;
        this.os = os;
        this.osName = osName;
        this.model = model;
        this.net = net;
        this.operators = operators;
        this.location = location;
        this.package_name = package_name;
        this.package_size = package_size;
        this.fill_register_msg_times = fill_register_msg_times;
        this.startup_times = startup_times;
        this.plat_id = plat_id;
        this.plat_name = plat_name;
        this.user_ip = user_ip;
        this.game_version = game_version;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAreId() {
        return areId;
    }

    public void setAreId(int areId) {
        this.areId = areId;
    }

    public String getUtma() {
        return utma;
    }

    public void setUtma(String utma) {
        this.utma = utma;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public int getOs() {
        return os;
    }

    public void setOs(int os) {
        this.os = os;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getOperators() {
        return operators;
    }

    public void setOperators(String operators) {
        this.operators = operators;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_size() {
        return package_size;
    }

    public void setPackage_size(String package_size) {
        this.package_size = package_size;
    }

    public String getFill_register_msg_times() {
        return fill_register_msg_times;
    }

    public void setFill_register_msg_times(String fill_register_msg_times) {
        this.fill_register_msg_times = fill_register_msg_times;
    }

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip(String user_ip) {
        this.user_ip = user_ip;
    }

    public int getStartup_times() {
        return startup_times;
    }

    public String getGame_version() {
        return game_version;
    }

    public void setGame_version(String game_version) {
        this.game_version = game_version;
    }

    public void setStartup_times(int startup_times) {
        this.startup_times = startup_times;
    }

    public int getPlat_id() {
        return plat_id;
    }

    public void setPlat_id(int plat_id) {
        this.plat_id = plat_id;
    }

    public String getPlat_name() {
        return plat_name;
    }

    public void setPlat_name(String plat_name) {
        this.plat_name = plat_name;
    }

    public String getPushChanelId() {
        return pushChanelId;
    }

    public void setPushChanelId(String pushChanelId) {
        this.pushChanelId = pushChanelId;
    }
}
