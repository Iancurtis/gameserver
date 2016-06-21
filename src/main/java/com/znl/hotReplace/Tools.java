package com.znl.hotReplace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Tools {


    /**
     * 从文件读取字节码
     * @param file
     * @return
     */
    public static byte[] readFormFile(File file) {
        if(file == null){
            throw new NullPointerException();
        }
        FileInputStream input = null;
        ByteArrayOutputStream output = null;
        try{
            input = new FileInputStream(file);
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n;
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(output != null){
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static File[] toFiles(String... fileNames) throws Exception{
        if(fileNames == null){
            throw new Exception("文件为空");
        }
        File[] files = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = new File(fileNames[i].trim());
            if(!files[i].exists()){
                throw new Exception("不存在文件:" + fileNames[i]);
            }
        }
        return files;
    }
}

