package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.hpe.ossm.alarmGenerator.generator.TemipAlarm;
import com.hpe.ossm.alarmGenerator.messages.QueryStatistics;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class DbWriteTester extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbWriteTester.class);

    //    private final String oc;
//    private final int num;
    private final String url;
    private final String user;
    private final String pwd;
    private List<String> sqls = null;

    private DbWriteTester(String url, String user, String pwd) {
        super();
//        this.oc = oc;
//        this.num = num;
        this.url = url;
        this.user = user;
        this.pwd = pwd;
    }

    public static Props props(String url, String user, String pwd) {
        return Props.create(DbWriteTester.class, () -> new DbWriteTester(url, user, pwd));
    }

    @Override
    public void preStart() {
//        System.out.println("DbWorker starting " + oc);
        LOGGER.info("DbWriteTester {} starting", System.currentTimeMillis());
        final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
        sqls = configurations.getStringList("writeSqls");
    }

    @Override
    public void postStop() {
//        System.out.println("DbWorker stopping " + oc);
        LOGGER.info("DbWriteTester {} stopping", System.currentTimeMillis());
    }

    private void work() throws Exception {
        int len = sqls.size();
        while (true) {
            for (int i = 0; i < len; i++) {
                String sql = sqls.get(i);
                long t0=System.currentTimeMillis();
                try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
                    try (Statement stat = conn.createStatement()) {
                        long t1 = System.currentTimeMillis();
                        Boolean r = stat.execute(sql);
                        long t = System.currentTimeMillis();
                        System.out.println((t1-t0)+"|"+(t-t1)+"|"+sql);
                    }
                }
            }
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
