package com.hpe.ossm.alarmGenerator.actor;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.alpakka.file.javadsl.LogRotatorSink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.hpe.ossm.alarmGenerator.messages.QueryStatistics;
import com.typesafe.config.Config;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
public class DBQueryManager extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBQueryManager.class);

    private final int numberOfTester;
    private final int numberOfOC;
    private final int numberOfMaxRecord;
    private transient int numOfRecord = 0;
    private final String url;
    private final String user;
    private final String pwd;
    private transient Path destinationDir;
    private transient String logFile;
    //    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final transient Materializer materializer = ActorMaterializer.create(getContext());
    private final JdbcConnectionPool cp;

    private double queryCostTotal=0d;
    private int    queryCount=0;
    private boolean stopping=false;

    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
//    private final String tableName=configurations.getString("tableName");
    private final boolean useSql=configurations.getBoolean("useSQL");
    private final String sql=configurations.getString("sql");
    private void printCollections(String content) {
        try {
            if (null == content) {
                LOGGER.error("No valid data in collections");
            } else {
                CompletionStage<Done> completion =
                        Source.from(Arrays.asList(content, "\n"))
                                .map(ByteString::fromString)
                                .runWith(LogRotatorSink.createFromFunction(timeBasedPathCreator), materializer);
                completion.thenRun(() -> {
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error data in in collections" + e);
        }

    }

    private DBQueryManager(String url, String user, String pwd, int numberOfTester, String path, int numberOfOC, int mr) {
        super();
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.numberOfTester = numberOfTester;
        this.numberOfOC = numberOfOC;
        this.numberOfMaxRecord = mr;
        String s;
        if(url.contains(("h2"))){
            s="h2DB-test-";
        }else{
            s="mariaDB-test-";
        }
        logFile=  s+ System.currentTimeMillis();
        try {
            if (null != path) {  //path is directory
                File file = new File(path);
                if (!file.exists()) {
                    if (file.mkdir()) {
                        LOGGER.debug("Succeed to make directory " + path);
                    }
                }
//                this.formatter = DateTimeFormatter.ofPattern("'" + "mariaDB-test" + "-'yyyy-MM-dd HH:mm'.log'");
                this.destinationDir = FileSystems.getDefault().getPath(path);
            }
            LOGGER.info("Log File: " + destinationDir + "/" + logFile);
            printCollections("Record Time" + " | " + "User ID" + " | " + "Time to get Connection" + " | " + "Time of Query" + " | " + "SQL" + "\n");
        } catch (Exception e) {
            LOGGER.error("FilePersistentActor Exception : " + e);
            getContext().stop(getSelf());
        }

//        System.out.println("url: "+url);
        if(url.contains("h2")){
//            this.cp= JdbcConnectionPool.create(url,user,pwd);
            this.cp=null;
        }else{
            this.cp=null;
        }
//        cp=null;
    }

    public static Props props(String url, String user, String pwd, int n, String destinationDir, int numberOfOC, int mr) {
        return Props.create(DBQueryManager.class, () -> new DBQueryManager(url, user, pwd, n, destinationDir, numberOfOC, mr));
    }

    private String[] sqls = {
            "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc1' and  additional_text like '%critical%'",
            "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc2' and  additional_text like '%Minor%'",
            "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc3' and  additional_text like '%Warning%'",
            "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc4' and additional_text like '%Indeterminate%'",

            "select * from temip_alarm where state='Acknowledge' and operation_context='dome_oc5' and additional_text like '%critical%'",
            "select * from temip_alarm where state='Acknowledge' and operation_context='dome_oc6' and additional_text like '%Minor%'",
            "select * from temip_alarm where state='Acknowledge' and operation_context='dome_oc7' and additional_text like '%Warning%'",
            "select * from temip_alarm where state='Acknowledge' and operation_context='dome_oc8' and additional_text like '%Indeterminate%'",

            "select * from temip_alarm where state='Terminated' and operation_context='dome_oc9' and additional_text like '%critical%'",
            "select * from temip_alarm where state='Terminated' and operation_context='dome_oc10' and additional_text like '%Minor%'",
            "select * from temip_alarm where state='Terminated' and operation_context='dome_oc11' and additional_text like '%Warning%'",
            "select * from temip_alarm where state='Terminated' and operation_context='dome_oc12' and additional_text like '%Indeterminate%'"

    };

    private String[] sqls2 = {
            "select * from temip_alarm where domain='ossv195_ns:.domain2' and additional_text  like '%Minor%' and operation_context='dome_oc23' and state='Outstanding'",
            "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc23' and domain='ossv195_ns:.domain2' and additional_text  like '%Minor%' "
    };

    private String getSql(int index) {
        if(useSql){
            return sql;
        }else{
            return "SELECT identifier FROM temip_alarm"+(index%5+1)+" WHERE state = 'Outstanding' and additional_text like '%Critical%' order by event_time desc, alarmIdentifier desc limit 0 ,500";
        }
//        return sqls[index % sqls.length];
//        return sqls[1];
//        return "select * from temip_alarm where state='Outstanding' and operation_context='dome_oc"+(index % numberOfOC)+"' and  additional_text like '%Minor%'";
//        return "select * from temip_alarm where match(additional_text)  AGAINST ('%Minor%') and operation_context='dome_oc" + (index % numberOfOC) + "' and state='Outstanding'  ";
//        return "select * from temip_alarm_p2 where (domain='ossv195_ns:.domain2' or domain='ossv195_ns:.domain1') and match(additional_text)  AGAINST ('%Minor%') or probable_cause='Fire'";
//        return sqls2[index % 2];
//        return " select * from "+tableName+" where state='Outstanding' and operation_context='dome_oc4' and additional_text like '%Minor%'";
//        return " select * from "+tableName+" where state='Outstanding' and operation_context='dome_oc4' and match(additional_text) against ('%Minor%')";
//        return "SELECT identifier,sourceIdentifier,event_time,alarmIdentifier,perceived_severity,operation_context,managed_object,alarm_type,additional_text,probable_cause,state,problem_status,sa_total,operator_note,acknowledgement_timestamp,acknowledgement_user_identifier,alarm_origin,clearance_report_flag,creation_timestamp,domain,escalatedAlarm,last_modification_timestamp,original_event_time,original_severity,previous_state,problemOccurrences,problem_information,specific_problems,target_entities,user_text,target,notificationIdentifier,outageFlag,correlGroup,termination_user_identifier,termination_timestamp,closedBy,releaseUserIdentifier,releaseTimestamp,closeUserIdentifier,closeTimestamp,handledUserIdentifier,handleTimestamp,clearanceTimeStamp,uniqueid FROM "+tableName+" WHERE (state = 'Outstanding') ORDER BY  event_time DESC ,  alarmIdentifier DESC ,  identifier DESC LIMIT 0 , 500";
    }

    @Override
    public void preStart() {
//        System.out.println("DBQueryManager starting " + numberOfTester);
        LOGGER.info("DBQueryManager {} starting", numberOfTester);

        for (int i = 0; i < numberOfTester; i++) {
            getContext().actorOf(DBQuery.props(i, url, user, pwd, getSql(i), getSelf(),cp).withDispatcher("q-dispatcher"));
        }
    }

    @Override
    public void postStop() {
//        System.out.println("DBQueryManager stopping " );
        LOGGER.info("DBQueryManager stopping. Average cost: " + (queryCostTotal/queryCount));
    }

    private final transient Creator<Function<ByteString, Optional<Path>>> timeBasedPathCreator =
            () -> {
//                final String[] currentFileName = new String[]{null};
                return (element) -> {
                    return Optional.of(destinationDir.resolve(logFile + ".txt"));
//                    String newName = timeID;
//                    if (newName.equals(currentFileName[0])) {
//                        System.out.println("aaaaaaaaa " + newName);
//                        return Optional.empty();
//                    } else {
//                        currentFileName[0] = newName;
//                        System.out.println("aaaaaaaaa1 " + newName);
//                        return Optional.of(destinationDir.resolve(newName));
//                    }
                };
            };


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryStatistics.class, c -> {
                    numOfRecord++;
                    if (numOfRecord >=numberOfMaxRecord && !stopping) {
                        stopping=true;
                        Thread.sleep(2500);
                        ActorHandler.shutdownSystem();
                    }else{
                        LOGGER.info(numOfRecord + ": [" + c.getId() + "]" + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql());
                        queryCostTotal = queryCostTotal+c.getCost_q();
                        queryCount=queryCount+1;
                        printCollections(LocalDateTime.now() + " | " + c.getId() + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql() + "\n");
                    }
                })
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
