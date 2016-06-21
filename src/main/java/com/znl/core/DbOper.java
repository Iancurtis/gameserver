package com.znl.core;

/**
 * Created by Administrator on 2016/3/2.
 */
public class DbOper {
    public String table;
    public String sql ;

    public DbOper(String table, String sql) {
        this.table = table;
        this.sql = sql;
    }
}
