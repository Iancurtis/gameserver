package com.znl.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/17.
 */
public class CreateClass {
    //包名
    private String strpackage ="";
    // 数据库表名
    private String tableName="";

    StringBuffer sb = new StringBuffer();
    Connection con = null;

    ResultSet res = null;
    ResultSetMetaData rsmd =null;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getStrpackage() {
        return strpackage;
    }

    public void setStrpackage(String strpackage) {
        this.strpackage = strpackage;
    }

    public void setConnection(Connection con){
        this.con = con;
    }

    public void execute() {
        String strsql = "select * from " + tableName;
        Statement st;
        try {

            DatabaseMetaData dbmd = con.getMetaData();


            st = con.createStatement();
            res = st.executeQuery(strsql);
            rsmd = res.getMetaData();

            String newstrpack = "";
            newstrpack = strpackage.replace(".", "/");
            String className = tableName;

            List<String> fieldTypeList = new ArrayList<>();
            List<String> fieldNameList = new ArrayList<>();

            //创建这个类文件strpackage
            File file = new File( System.getProperty("user.dir") + "/src/main/java/"+"/"+newstrpack+"/"+className+".java");
            sb.append("package "+strpackage+";\n");
            sb.append("import com.znl.base.BaseLog;\n");
            sb.append("/*\n *");
            sb.append("auto export class：\n *@author woko\n */\n");
            sb.append("public class " + className + " extends BaseLog{");
            sb.append("\n");

            for(int i = 1;i <= rsmd.getColumnCount();i++){
                String type = typename(rsmd.getColumnTypeName(i));
                //增加注释
                ResultSet rs = dbmd.getColumns(null,"%", tableName,"%");
                while(rs.next()){
                    if(rs.getString("COLUMN_NAME").equals(rsmd.getColumnName(i))){
                        sb.append("\t/***" + rs.getString("REMARKS") + "***/\n");
                    }
//                    System.out.println(rs.getString("COLUMN_NAME")+"----"+rs.getString("REMARKS") + "----");
                }
                fieldTypeList.add(type);
                fieldNameList.add(rsmd.getColumnName(i));
                sb.append("\tprivate ");
                sb.append(type + " ");
                sb.append(rsmd.getColumnName(i) + " = " + getDefaultValue(rsmd.getColumnTypeName(i)) + " ;");
                sb.append("\n");
                //打印get方法
//                sb.append("\t//get方法\n");
                sb.append("\tpublic " + type + " get" + upFirstStr(rsmd.getColumnName(i)) + "(){\n");
                sb.append("\t  return " + rsmd.getColumnName(i) + ";\n");
                sb.append("\t}\n");

                //打印set方法
//                sb.append("\t//set方法\n");
                //
                sb.append("\tpublic " + "void" + " set" + upFirstStr(rsmd.getColumnName(i)) + "(" + type + " " + rsmd.getColumnName(i) + ")" + "{\n");
                sb.append("\tthis." + rsmd.getColumnName(i) + " = " + rsmd.getColumnName(i) + ";\n");
                sb.append("\t}\n\n");
            }

            //构造方法
            sb.append("\tpublic " + className + "() {\n\t}\n\n");

            sb.append("\tpublic " + className + "(");
            int index = 0;
            for(String fieldType : fieldTypeList){
                if(index == fieldTypeList.size() - 1){
                    sb.append(fieldType + " " + fieldNameList.get(index) + "){\n");
                }else{
                    sb.append(fieldType + " " + fieldNameList.get(index) + ", ");
                }
                index++;
            }

            for(String fieldName : fieldNameList){
                sb.append("\t\tthis." + fieldName + " = " + fieldName + ";\n");
            }

            sb.append("\t}\n\n");



            sb.append("}");
            String strsb = sb.toString();
            //创建一个FileWriter对象
            FileWriter fw = new FileWriter(file);
            //创建一个BufferedWriter对象
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strsb);
            bw.newLine();
            //关闭文件流
            bw.flush();
            fw.close();

            con.close();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }
    }


    public String typename(String typename){
        typename = typename.toLowerCase();
        String tystr=null;
        if(typename.equals("varchar")){
            tystr="String";
        }else if(typename.equals("int") || typename.equals("tinyint")){
            tystr="Integer";
        }else if(typename.equals("bigint")){
            tystr="Long";
        }
        else{
            tystr="Object";
        }
        return tystr;
    }

    private String getDefaultValue(String typename){
        typename = typename.toLowerCase();
        String defaultValue = "null";
        if(typename.equals("varchar")){
            defaultValue = "\"\"";
        }else if(typename.equals("int") || typename.equals("tinyint")){
            defaultValue="0";
        }else if(typename.equals("bigint")){
            defaultValue = "0l";
        }
        else{
            defaultValue="null";
        }

        return defaultValue;
    }

    public String upFirstStr(String str){
        str = str.substring(0, 1).toUpperCase() + str.substring(1);
        return str;
    }

}
