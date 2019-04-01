package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.hpe.ossm.alarmGenerator.messages.QueryStatistics;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.h2.jdbcx.JdbcConnectionPool;

public class DBQuery extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBQuery.class);
    private final String url;
    private final String user;
    private final String pwd;
    private final String sql;
    private final ActorRef manager;
    private int id;
    private transient int count = 0;
    private transient long T;
    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
    private final int interval = configurations.getInt("queryInterval");
    private final int stopSelf = configurations.getInt("stopSelf");
    private final JdbcConnectionPool cp;

    private DBQuery(int id, String url, String user, String pwd, String sql, ActorRef manager) {
        super();
        this.id = id;
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.sql = sql;
        this.manager = manager;
        cp = null;
    }

    private DBQuery(int id, String url, String user, String pwd, String sql, ActorRef manager, JdbcConnectionPool cp) {
        super();
        this.id = id;
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.sql = sql;
        this.manager = manager;
        this.cp = cp;
    }

    public static Props props(int id, String url, String user, String pwd, String sql, ActorRef manager) {
        return Props.create(DBQuery.class, () -> new DBQuery(id, url, user, pwd, sql, manager));
    }

    public static Props props(int id, String url, String user, String pwd, String sql, ActorRef manager, JdbcConnectionPool cp) {
        return Props.create(DBQuery.class, () -> new DBQuery(id, url, user, pwd, sql, manager, cp));
    }

    @Override
    public void preStart() {
//        System.out.println("DBQuery starting " + sql);
        LOGGER.info("DBQuery {} starting", id);
//        try {
//            conn = DriverManager.getConnection(url, user, pwd);
//        } catch (Exception e) {
//            LOGGER.error("Failed to create the connection to DB. " + e);
//        }
        T = System.currentTimeMillis();
        getContext().system().scheduler().schedule(
                scala.concurrent.duration.Duration.create(0, TimeUnit.SECONDS),
                scala.concurrent.duration.Duration.create(interval, TimeUnit.SECONDS),
                self(),
                "test",
                getContext().dispatcher(),
                ActorRef.noSender());
    }

    @Override
    public void postStop() {
//        System.out.println("DBQuery stopping " + sql);
        LOGGER.info("DBQuery {} stopping", id);
//        if (null != conn) {
//            try {
//                conn.close();
//            } catch (Exception e) {
//                LOGGER.error("DB Error:" + e.getMessage());
//            }
//        }
    }

    private void work() throws Exception {
        long t0 = System.currentTimeMillis();
        long t1, t;
        if (cp == null) {
            try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
                LOGGER.info(id + "++++++++++++++++++++" + (null != conn));
                try (Statement stat = conn.createStatement()) {
                    count = count + 1;
                    t1 = System.currentTimeMillis();
                    try (ResultSet rs = stat.executeQuery(sql)) {
                        t = System.currentTimeMillis();
//                    if(count==1){
//                        int rownum=0;
//                        while(rs.next())
//                        {
//                            rownum++;
//                        }
//                        System.out.println(">>>>>>> " + t+" | "+rownum+" | "+sql  );
//                    }

                        manager.tell(new QueryStatistics(id, sql, t1 - t0, t - t1, ((System.currentTimeMillis() - T) / interval / 1000), count, LocalDateTime.now()), ActorRef.noSender());

                        LOGGER.info(((System.currentTimeMillis() - T) / interval / 1000) + ":" + count + ": [" + id + "]" + " | " + (t1 - t0) + " | " + (t - t1) + " | " + sql);

                        if (stopSelf > 0) {
                            if (count >= stopSelf) {
                                getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                            }
                        }
                    }
                }
                LOGGER.info(id + "-----------------------" + (System.currentTimeMillis() - t));
            }
        } else {
            try (Connection conn = cp.getConnection()) {
                LOGGER.info(id + "++++++cp++++++++++++++" + (null != conn));
                try (Statement stat = conn.createStatement()) {
                    count = count + 1;
                    t1 = System.currentTimeMillis();
                    try (ResultSet rs = stat.executeQuery(sql)) {
                        t = System.currentTimeMillis();
                        manager.tell(new QueryStatistics(id, sql, t1 - t0, t - t1, ((System.currentTimeMillis() - T) / interval / 1000), count, LocalDateTime.now()), ActorRef.noSender());
                        LOGGER.info(((System.currentTimeMillis() - T) / interval / 1000) + ":" + count + ": [" + id + "]" + " | " + (t1 - t0) + " | " + (t - t1) + " | " + sql);
                        if (stopSelf > 0) {
                            if (count >= stopSelf) {
                                getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                            }
                        }
                    }
                }
            }
            LOGGER.info(id + "------------cp-----------" + (System.currentTimeMillis() - t));
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("test", s -> {
                    work();
                })
                .matchEquals("stop", s -> {

                })
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
