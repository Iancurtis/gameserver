package com.znl.framework.socket.base;

import org.apache.mina.core.session.IoSession;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/10/22.
 */
public class FireWall {
    private static FireWall fireWall = null;//游戏世界实例
    public synchronized static FireWall getInstance(){
        if(fireWall==null){
            fireWall = new FireWall();
        }
        return fireWall;
    }

    public ConcurrentHashMap<String, Long> ipLog = new ConcurrentHashMap<String, Long>();
    public ConcurrentHashMap<String, Long> defentMap = new ConcurrentHashMap<String, Long>();
    /***白名单***/
    String[] localIp = {
            "192.168."
    };

    public void pushToBlackList(IoSession session){
        if(session.getAttribute("ip") == null){
            putIPToSession(session);
        }
        String ip = (String) session.getAttribute("ip");
        for(String wirteIp : localIp){
            if(ip.startsWith(wirteIp)){
                return ;	//白名单不限制
            }
        }
        if(ipLog.containsKey(ip) == false){
            ipLog.put(ip, 0l);
        }
        Long times = ipLog.get(ip)+1  ;
        ipLog.put(ip, times);
        System.out.println(ip+"错误连接，系统累加黑名单，次数"+times);
    }

    public boolean checkInBlackList(IoSession session){
        if(checkInfoFlood(session)){
            return true;
        }
        if(session.getAttribute("ip") == null){
            putIPToSession(session);
        }
        String ip = (String) session.getAttribute("ip");
        Long times = ipLog.get(ip) ;
        if(times != null && times > 100){
            System.out.println(ip+"已被防火墙拦截");
            return true;
        }
        return false;
    }

    private String putIPToSession(IoSession session){
        SocketAddress addr = session.getRemoteAddress();
        String[] addrTemp = addr.toString().split(":");
        String ip = addrTemp[0].replace("/", "");
        session.setAttribute("ip", ip);
        return ip;
    }

    private boolean checkInfoFlood(IoSession session) {
        try{
            long now = System.currentTimeMillis();
            Integer conectTimes = (Integer) session.getAttribute("conectTimes");
            Long lastConnectTime = (Long) session.getAttribute("lastConnectTime");
            Integer intervalTime = (Integer) session.getAttribute("intervalTime");
            if(conectTimes == null){
                session.setAttribute("conectTimes", 0);
                conectTimes = 0;
            }
            if(lastConnectTime == null){
                session.setAttribute("lastConnectTime", now);
                lastConnectTime = now;
            }
            if(intervalTime == null){
                session.setAttribute("intervalTime", 0);
                intervalTime = 0;
            }
            conectTimes++;
            intervalTime += (int)(now - lastConnectTime.longValue());
            if(conectTimes > 15){
                if( intervalTime < 1000){
                    session.close(true);
                    System.out.println("防火墙断开请求过于频繁的session");
                    return true;
                }else{
                    conectTimes = 0;
                    intervalTime = 0;
                }
            }
            session.setAttribute("conectTimes", conectTimes);
            session.setAttribute("lastConnectTime", now);
            session.setAttribute("intervalTime", intervalTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }
}
