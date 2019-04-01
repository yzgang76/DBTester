package com.hpe.ossm.h2;

import java.sql.*;

import org.h2.jdbcx.JdbcConnectionPool;

public class Test {
    public static void main(String[] args) throws Exception {
        JdbcConnectionPool cp = JdbcConnectionPool.create(
                "jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_DELAY=-1;MULTI_THREADED=1;LOCK_MODE=3;LOG=0;UNDO_LOG=0",
                "sa", "");
        for (int i = 0; i <50; i++) {
            Connection conn = cp.getConnection();
            System.out.println(i+": "+(conn==null));
            conn.createStatement().executeQuery("select 1");
            conn.close();
        }
        cp.dispose();
    }

}
