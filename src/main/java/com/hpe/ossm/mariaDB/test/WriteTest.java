package com.hpe.ossm.mariaDB.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.hpe.ossm.alarmGenerator.actor.DbWriteTester;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteTest.class);
    public static void main(String[] args) {
        try {
            final ActorSystem actorSystem = ActorHandler.getInstance().getActorSystem();
            final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
            final String url=configurations.getString("url");
            final String user=configurations.getString("user");
            final String pwd=configurations.getString("pwd");
            final String logPath=configurations.getString("logPath");
//            final int ocNum=configurations.getInt("numOfOC");
//            final int alarmNum=configurations.getInt("numOfAlarmPerOC");


//            System.out.println(url+"|"+ocNum+"|"+alarmNum);

            ActorRef r = actorSystem.actorOf(DbWriteTester.props(url,user,pwd));
//            r.tell("start",ActorRef.noSender());

        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
            LOGGER.error("Error: "+ e.getMessage());
            System.exit(-1);
        }
    }
}
