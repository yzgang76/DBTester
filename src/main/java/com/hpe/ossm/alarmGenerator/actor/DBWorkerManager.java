package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBWorkerManager extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBWorkerManager.class);

    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
    private final int ocNum=configurations.getInt("numOfOC");
    private final int alarmNum=configurations.getInt("numOfAlarmPerOC");
    private final String url=configurations.getString("url");
    private final String user=configurations.getString("user");
    private final String pwd=configurations.getString("pwd");
    private int stopped=0;
    private DBWorkerManager() {
        super();

    }

    public static Props props() {
        return Props.create(DBWorkerManager.class, DBWorkerManager::new);
    }


    @Override
    public void preStart() {
        LOGGER.info("DBWorkerManager {} starting", ocNum);

        for(int i=1;i<=ocNum;i++){
            ActorRef r=getContext().actorOf(DbWorker.props("perf_oc"+i, alarmNum,url,user,pwd,getSelf()).withDispatcher("q-dispatcher"));
            r.tell("start",getSelf());
        }
    }

    @Override
    public void postStop() {
        LOGGER.info("DBWorkerManager stopping");
    }

    private void monitor(){
        stopped=stopped+1;
        if(stopped>=ocNum){
            LOGGER.info("All DB Worker stopped. Stopping Actor System ...");
            ActorHandler.shutdownSystem();
        }
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("stopped",s->monitor())
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
