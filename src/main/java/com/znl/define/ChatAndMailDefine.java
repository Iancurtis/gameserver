package com.znl.define;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/11/30.
 */
public class ChatAndMailDefine {
    /**世界聊天池容量**/
    public static final int MAX_CHAT_WORLD = 10000;
    public static final int MAX_CHAT_LEGION = 1000;

    public static final int CLIENT_GET_CHAT_NUM = 50;


    /**聊天类型：私聊**/
    public static final int CHAT_TYPE_PRIVATE = 0;
    /**聊天类型：世界**/
    public static final int CHAT_TYPE_WORLD = 1;
    /**聊天类型：军团**/
    public static final int CHAT_TYPE_LEGION = 2;


    public static final String CHAT_NODE_NAME_WORLD = "world";
    public static final String CHAT_NODE_NAME_LEGION = "legion";


    /**邮件状态：未读**/
    public static final int MAIL_STATE_UNREAD = 0;
    /**邮件状态：已读**/
    public static final int MAIL_STATE_READ = 1;

    /**邮件类型：系统**/
    public static final int MAIL_TYPE_SYSTEM = 1;
    /**邮件类型：发件箱**/
    public static final int MAIL_TYPE_SEND = 2;
    /**邮件类型：邮件**/
    public static final int MAIL_TYPE_INBOX = 3;
    /**邮件类型：报告**/
    public static final int MAIL_TYPE_REPORT = 4;

    /**屏蔽类型：邮件**/
    public static final int SHIELD_TYPE_MAIL = 0;
    /**屏蔽类型：聊天**/
    public static final int SHIELD_TYPE_CHAT = 1;

    public static final int MAIL_SIZE = 100;
    /**已提取**/
    public static final int MAIL_HAVE_EXTRACT = 1;
    /**未提取**/
    public static final int MAIL_NOT_EXTRACT = 0;



    public static final int WORLD_MAX=599;//世界最大边界

    public static final int WORLD_MAGNIFY=15;//观察的最大距离

    public static final int WORLD_MAGNIFY1=100;//

    public static final int PEOPLE_RANDOM=5;//最大人数
    public static final int PEOPLE_RESOUCE=5;//最资源数
    public final static List<Integer> RANDOM_RESOUCE1 = Arrays.asList(4,6,8,10);//放大镜资源随机参数1
    public final static List<Integer> RANDOM_RESOUCE2 = Arrays.asList(0,1);//放大镜资源随机参数2


    //最近最大人数
    public static final int LATERMAX=20;//

   public static final int ShARE_TYPE_WORLD=1;//世界分享


    public static final int CHAT_TYPE_LEGIONENLIAT = 5;//军团招募类型
}
