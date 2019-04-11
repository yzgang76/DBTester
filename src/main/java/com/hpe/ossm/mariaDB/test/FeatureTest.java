package com.hpe.ossm.mariaDB.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.hpe.ossm.alarmGenerator.actor.DBQueryManager;
import com.hpe.ossm.alarmGenerator.actor.ViewMockup;
import com.hpe.ossm.alarmGenerator.actor.ViewMockupRecorder;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureTest.class);

    public static void main(String[] args) {
        try {
            final ActorSystem actorSystem = ActorHandler.getInstance().getActorSystem();
            final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
            final String url=configurations.getString("url");
            final String user=configurations.getString("user");
            final String pwd=configurations.getString("pwd");
            final String logPath=configurations.getString("logPath");
            final int testerNum=configurations.getInt("numOfTester");
            final int ocNum=configurations.getInt("numOfOC");
            final int mr=configurations.getInt("maxRecord");
            final int nViews=configurations.getInt("numberOfViews");
//            final int ocNum=configurations.getInt("numOfOC");
//            final int alarmNum=configurations.getInt("numOfAlarmPerOC");


//            System.out.println(url+"|"+ocNum+"|"+alarmNum);
            final boolean singleTest=configurations.getBoolean("singleTest");
            if(singleTest){
                ActorRef r = actorSystem.actorOf(DBQueryManager.props(url,user,pwd,testerNum,logPath,ocNum,mr));
            }else{
                ActorRef r = actorSystem.actorOf(ViewMockupRecorder.props());
                for(int i=0;i<nViews;i++){
                    actorSystem.actorOf(ViewMockup.props(i,r));
                }
            }


        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
            LOGGER.error("Error: "+ e.getMessage());
            System.exit(-1);
        }
    }
}
