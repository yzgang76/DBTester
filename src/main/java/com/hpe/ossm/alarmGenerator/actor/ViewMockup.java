package com.hpe.ossm.alarmGenerator.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewMockup extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewMockup.class);

    private transient int numOfRecord = 0;
    private final int id;
//    private transient Path destinationDir;
//    private final String logFile = "view-test-" + System.currentTimeMillis();
    //    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private final transient Materializer materializer = ActorMaterializer.create(getContext());


    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
    private final String url = configurations.getString("url");
    private final String user = configurations.getString("user");
    private final String pwd = configurations.getString("pwd");
    private final JdbcConnectionPool cp;
//    private final int testerNum = configurations.getInt("numOfTester");
    private final ConfigObject view = configurations.getObject("viewSqls");
//    private final String path = configurations.getString("logPath");
    private final ActorRef recorder;
    /*private void printCollections(String content) {
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

    }*/

    private ViewMockup(int id,ActorRef recorder) {
        super();
        this.id=id;
        this.recorder=recorder;
        System.out.println("url: "+url);
        if(url.contains("h2")){
            this.cp=JdbcConnectionPool.create(url,user,pwd);
        }else{
            this.cp=null;
        }
//        try {
//            if (null != path) {  //path is directory
//                File file = new File(path);
//                if (!file.exists()) {
//                    if (file.mkdir()) {
//                        LOGGER.debug("Succeed to make directory " + path);
//                    }
//                }
////                this.formatter = DateTimeFormatter.ofPattern("'" + "mariaDB-test" + "-'yyyy-MM-dd HH:mm'.log'");
//                this.destinationDir = FileSystems.getDefault().getPath(path);
//            }
//            LOGGER.info("Log File: " + destinationDir + "/" + logFile);
//            printCollections("Record Time" + " | " + "User ID" + " | " + "Time to get Connection" + " | " + "Time of Query" + " | " + "SQL" + "\n");
//        } catch (Exception e) {
//            LOGGER.error("FilePersistentActor Exception : " + e);
//            getContext().stop(getSelf());
//        }
    }

    public static Props props(int id,ActorRef recorder) {
        return Props.create(ViewMockup.class, ()->new ViewMockup(id,recorder));
    }

    @Override
    public void preStart() {
//        System.out.println("DBQueryManager starting " + numberOfTester);
        LOGGER.info("ViewMockup  starting");

        if(null!=view){
            view.toConfig().getStringList("statistics").forEach(s->
                    getContext().actorOf(DBQuery.props(id, url, user, pwd, s, recorder,cp).withDispatcher("q-dispatcher"))
            );
            view.toConfig().getStringList("dc").forEach(s->
                    getContext().actorOf(DBQuery.props(id, url, user, pwd, s, recorder,cp).withDispatcher("q-dispatcher"))
            );
            view.toConfig().getList("alarm").forEach(l->{
                l.atPath("0").getList("0").atPath("0").getStringList("0").forEach(s->
                    getContext().actorOf(DBQuery.props(id, url, user, pwd, s, recorder,cp).withDispatcher("q-dispatcher"))
                );
            });
        }
    }

    @Override
    public void postStop() {
//        System.out.println("DBQueryManager stopping " );
        LOGGER.info("ViewMockup stopping");
        if(null!=cp){
            cp.dispose();
        }
    }

//    private final transient Creator<Function<ByteString, Optional<Path>>> timeBasedPathCreator =
//            () -> {
////                final String[] currentFileName = new String[]{null};
//                return (element) -> Optional.of(destinationDir.resolve(logFile + ".txt"));
//            };


    @Override
    public Receive createReceive() {
        return receiveBuilder()
//                .match(QueryStatistics.class, c -> {
//                    LOGGER.info(numOfRecord + ": [" + c.getId() + "]" + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql());
//                    recorder.tell(LocalDateTime.now() + " | " + c.getId() + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql() + "\n");
//                    numOfRecord++;
////                    if (numOfRecord > numberOfMaxRecord) {
////                        Thread.sleep(2500);
////                        ActorHandler.shutdownSystem();
////                    } else {
////                        LOGGER.info(numOfRecord + ": [" + c.getId() + "]" + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql());
////                        printCollections(LocalDateTime.now() + " | " + c.getId() + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql() + "\n");
////                    }
//                })
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
