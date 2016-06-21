package com.znl.event;

import com.znl.event.annotation.FixedTimeAnnotation;
import com.znl.event.annotation.ZeroTimeAnnotation;
import com.znl.event.base.BaseFixedTimeEvent;
import com.znl.event.base.BaseZeroEvent;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/6/16.
 */
public class EventCache {

    private static  final  String EVENT_PACKAGE="com.znl.event.impl";
    /**
     * 定时事件集合 <触发编号，事件集合>
     */
    public static HashMap<Integer,List<BaseFixedTimeEvent>> fixedTimeMap = new HashMap<Integer, List<BaseFixedTimeEvent>>();

    /**
     * 个人零点处理对象集合
     */
    public static List<BaseZeroEvent> zeroHandlers=new ArrayList<>() ;

    /**
     * 初始化事件缓存
     */
    public static void  registerEvent(){
        try {
            List<Class<?>> classes= getClasses(EVENT_PACKAGE);
            if(classes.isEmpty())return;
            for(Class c:classes){
                Annotation[] annotations= c.getAnnotations();
                for(Annotation annotation:annotations){
                    if(annotation instanceof FixedTimeAnnotation){
                        FixedTimeAnnotation fixedTimeAnnotation=(FixedTimeAnnotation)annotation;
                        int []actionTime=fixedTimeAnnotation.actionTimes();
                        for(int time:actionTime){
                            List<BaseFixedTimeEvent>baseFixedTimeEventList= fixedTimeMap.get(time);
                            if(baseFixedTimeEventList!=null){
                                baseFixedTimeEventList.add((BaseFixedTimeEvent)c.newInstance());
                            }else{
                                baseFixedTimeEventList=new ArrayList<>();
                                baseFixedTimeEventList.add((BaseFixedTimeEvent)c.newInstance());
                                fixedTimeMap.put(time,baseFixedTimeEventList);
                            }
                        }
                    }else if(annotation instanceof ZeroTimeAnnotation){
                        zeroHandlers.add((BaseZeroEvent)c.newInstance());
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取某个包下的所有类
     * @param pack
     * @return
     */
    public static List<Class<?>> getClasses(String pack) {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }
            }
            System.err.println(classes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  classes;
    }


    /**
     * 以文件的形式来获取包下的所有Class
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes){
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        //循环所有文件
        for (File file : dirfiles) {
            //如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        classes);
            }
            else {
                //如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static   void  main (String[]args){
        registerEvent();
    }
}

