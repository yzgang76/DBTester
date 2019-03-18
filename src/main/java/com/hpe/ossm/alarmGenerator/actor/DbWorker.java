package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.generator.TemipAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import akka.actor.PoisonPill;

public class DbWorker extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbWorker.class);

    private final String oc;
    private final int num;
    private final String url;
    private final String user;
    private final String pwd;
    private transient Connection conn;
    private int counter = 0;
    private final TemipAlarm t = new TemipAlarm();


    private DbWorker(String oc, int num, String url, String user, String pwd) {
        super();
        this.oc = oc;
        this.num = num;
        this.url = url;
        this.user = user;
        this.pwd = pwd;
    }

    public static Props props(String oc, int num, String url, String user, String pwd) {
        return Props.create(DbWorker.class, () -> new DbWorker(oc, num, url, user, pwd));
    }

    @Override
    public void preStart() {
//        System.out.println("DbWorker starting " + oc);
        LOGGER.info("DbWorker {} starting", oc, System.currentTimeMillis());
        try {
            conn = DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            LOGGER.error("Failed to create the connection to DB. " + e);
        }
    }

    @Override
    public void postStop() {
//        System.out.println("DbWorker stopping " + oc);
        LOGGER.info("DbWorker {} stopping", oc, System.currentTimeMillis());
        if (null != conn) {
            try {
                conn.close();
            } catch (Exception e) {
                LOGGER.error("DB Error:" + e.getMessage());
            }
        }
    }

    private void work() {
        try {
            if (null == conn) {
                LOGGER.error("Error: no db connection");
                return;
            }
            Statement stat = conn.createStatement();
            String sql;
            for (int i = 0; i < num; i++) {
                sql = t.getAlarmSql(oc);
//                System.out.println("sql= " + sql);
                try {
                    stat.execute(sql);
                    counter++;
                } catch (Exception e) {
                   System.out.println("Insert Error:"+e.getMessage());
                }
                if (counter % 100 == 0) {
                    System.out.println("DBWork " + oc + ":" + counter);
                }
            }
            stat.close();
            getSelf().tell(PoisonPill.getInstance(), getSelf());
        } catch (Exception e) {
            System.out.println("Error22: " + e.getMessage());
            LOGGER.error("Error: " + e.getMessage());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("start", s -> {
                    work();
                })
                .matchEquals("stop", s -> {

                })
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
