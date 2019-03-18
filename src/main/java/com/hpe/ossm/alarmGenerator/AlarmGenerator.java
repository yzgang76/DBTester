package com.hpe.ossm.alarmGenerator;


import com.hpe.ossm.alarmGenerator.actor.DbWorker;
import com.typesafe.config.Config;

import akka.actor.ActorRef;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmGenerator.class);

    public static void main(String[] args) {
        try {

            final ActorSystem actorSystem = ActorHandler.getInstance().getActorSystem();
            final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
            final String url=configurations.getString("url");
            final String user=configurations.getString("user");
            final String pwd=configurations.getString("pwd");
            final int ocNum=configurations.getInt("numOfOC");
            final int alarmNum=configurations.getInt("numOfAlarmPerOC");


            System.out.println(url+"|"+ocNum+"|"+alarmNum);

            for(int i=1;i<=ocNum;i++){
                ActorRef r = actorSystem.actorOf(DbWorker.props("perf_oc"+i, alarmNum,url,user,pwd));
                r.tell("start",ActorRef.noSender());
            }

        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
            LOGGER.error("Error: "+ e.getMessage());
            System.exit(-1);
        }
    }
}
