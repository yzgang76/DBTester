package com.hpe.ossm.h2;

import java.sql.*;
import java.time.LocalDateTime;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.hpe.ossm.alarmGenerator.messages.QueryStatistics;
import org.h2.jdbcx.JdbcConnectionPool;

public class Test {
    public static void main(String[] args) throws Exception {
//        JdbcConnectionPool cp = JdbcConnectionPool.create(
//                "jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_DELAY=-1;MULTI_THREADED=1;LOCK_MODE=3;LOG=0;UNDO_LOG=0",
//                "sa", "");
//        for (int i = 0; i <50; i++) {
//            Connection conn = cp.getConnection();
//            System.out.println(i+": "+(conn==null));
//            conn.createStatement().executeQuery("select 1");
//            conn.close();
//        }
//        cp.dispose();
        String url = "jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_DELAY=-1;MULTI_THREADED=1;LOCK_MODE=3;LOG=0;UNDO_LOG=0";
        String user = "sa";
        String pwd = "";
        long t0, t1, t2;
        try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
//                LOGGER.info(id + "++++++++++++++++++++" + (null != conn));
            try (Statement stat = conn.createStatement()) {
                t0 = System.currentTimeMillis();
                try (ResultSet rs = stat.executeQuery("select * from temip_alarm")) {
                    t1 = System.currentTimeMillis();
                    while (rs.next()) {
                        rs.getString("identifier");
                    }
                }
                t2 = System.currentTimeMillis();
            }
            System.out.println("aaa" + (t1 - t0) + " | " + (t2 - t1) + " | " + t0 + " | " + t1 + " | " + t2);
//                LOGGER.info(id + "-----------------------" + (System.currentTimeMillis() - t));
        }
    }
}
