package com.znl.hotReplace;


import com.cndw.ssm.javaagent.AgentMain;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/19.
 */
public class HotReplaceClass {

    /**
     * JAVA热替换类实例
     * @param packageName	类的包名
     * @param classFileNames	类文件数组，修改类时同时修改内部类需要多个文件
     * @throws Exception
     */
    public static void hotReplaceClass(String packageName, String... classFileNames) throws Exception {
        Instrumentation inst = AgentMain.getInst();
        if(inst == null){
            throw new Exception("没有Instrumentation实例");
        }
        File[] files = Tools.toFiles(classFileNames);
        List<String> classNames = new ArrayList<>();

        ClassDefinition[] classDefinitions = new ClassDefinition[files.length];
        for (int i = 0; i < files.length; i++) {
            byte[] classBytes = Tools.readFormFile(files[i]);

            String classSimpleName = getClassSimpleName(files[i]);
            String className;
            if(packageName == null || (packageName = packageName.trim()).isEmpty()){
                className = classSimpleName;
            }else{
                className = packageName + "." + classSimpleName;
            }
            classNames.add(className);
            System.out.println(className);
            Class<?> clazz = Class.forName(className);
            classDefinitions[i] = new ClassDefinition(clazz, classBytes);
        }

        inst.redefineClasses(classDefinitions);

        System.out.println("替换类成功,classNames" + classNames);
    }


    private static String getClassSimpleName(File file){
        String name = file.getName();
        int extLen = ".class".length();
        int endIndex = name.length() - extLen;
        if(endIndex <= 0){
            return null;
        }
        return name.substring(0, endIndex);
    }

}
