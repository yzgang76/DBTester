package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.messages.QueryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class DBQuery extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBQuery.class);
    private final String url;
    private final String user;
    private final String pwd;
    private final String sql;
    private final ActorRef manager;
    private int id;
//    private transient Connection conn;
    private int count=0;

    private DBQuery(int id,String url, String user, String pwd,String sql,ActorRef manager) {
        super();
        this.id=id;
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.sql=sql;
        this.manager=manager;
    }
    public static Props props(int id,String url, String user, String pwd,String sql,ActorRef manager) {
        return Props.create(DBQuery.class, () -> new DBQuery(id, url, user, pwd,sql,manager));
    }

    @Override
    public void preStart() {
//        System.out.println("DBQuery starting " + sql);
        LOGGER.info("DBQuery {} starting",  sql);
//        try {
//            conn = DriverManager.getConnection(url, user, pwd);
//        } catch (Exception e) {
//            LOGGER.error("Failed to create the connection to DB. " + e);
//        }

        getContext().system().scheduler().schedule(
                scala.concurrent.duration.Duration.create(0, TimeUnit.SECONDS),
                scala.concurrent.duration.Duration.create(5, TimeUnit.SECONDS),
                self(),
                "test",
                getContext().dispatcher(),
                ActorRef.noSender());
    }

    @Override
    public void postStop() {
//        System.out.println("DBQuery stopping " + sql);
        LOGGER.info("DBQuery {} stopping", sql, System.currentTimeMillis());
//        if (null != conn) {
//            try {
//                conn.close();
//            } catch (Exception e) {
//                LOGGER.error("DB Error:" + e.getMessage());
//            }
//        }
    }
    private void work () throws Exception{
        long t0=System.currentTimeMillis();
        try(Connection conn = DriverManager.getConnection(url, user, pwd)){
            try(Statement stat = conn.createStatement()){
                count++;
                long t1=System.currentTimeMillis();
                try(ResultSet rs=stat.executeQuery(sql)){
                    long t=System.currentTimeMillis();
//                    if(count==1){
//                        int rownum=0;
//                        while(rs.next())
//                        {
//                            rownum++;
//                        }
//                        System.out.println(">>>>>>> " + t+" | "+rownum+" | "+sql  );
//                    }

                    manager.tell(new QueryStatistics(id,sql,t1-t0,t-t1),ActorRef.noSender());
                }
            }
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
