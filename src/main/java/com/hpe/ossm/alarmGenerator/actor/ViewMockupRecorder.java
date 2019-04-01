package com.hpe.ossm.alarmGenerator.actor;

import akka.Done;
import akka.actor.AbstractActor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ViewMockupRecorder extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewMockupRecorder.class);

    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
    private final String logFile = "view-test-" + System.currentTimeMillis();
    private final String path = configurations.getString("logPath");
    private transient Path destinationDir;
    private final transient Materializer materializer = ActorMaterializer.create(getContext());
    private transient int numOfRecord = 0;

    private ViewMockupRecorder(){
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
            printCollections("Record Time|ts|Mail Box Size|Sub Count|User ID|Time to get Connection|Time of Query|SQL\n");
        } catch (Exception e) {
            LOGGER.error("FilePersistentActor Exception : " + e);
            getContext().stop(getSelf());
        }
    }
    public static Props props() {
        return Props.create(ViewMockupRecorder.class, ViewMockupRecorder::new);
    }

    private final transient Creator<Function<ByteString, Optional<Path>>> timeBasedPathCreator =
            () -> {
//                final String[] currentFileName = new String[]{null};
                return (element) -> Optional.of(destinationDir.resolve(logFile + ".txt"));
            };
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

    @Override
    public void preStart() {
//        System.out.println("DBQueryManager starting " + numberOfTester);
        LOGGER.info("ViewMockupRecorder  starting");
    }

    @Override
    public void postStop() {
//        System.out.println("DBQueryManager stopping " );
        LOGGER.info("ViewMockupRecorder stopping");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryStatistics.class, c -> {
                    LOGGER.info(">>>>>>>>"+c.getSize() + " | " +c.getCount() + " | " + c.getId() +"|"+ (c.getCost_a()+c.getCost_q()) + " | " + c.getSql());
                    printCollections(LocalDateTime.now() + " | " +c.getTs()+ " | " +c.getSize() + " | " +c.getCount()+ " | " + c.getId() + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql() + "\n");
                    numOfRecord=numOfRecord+1;
//                    if (numOfRecord > numberOfMaxRecord) {
//                        Thread.sleep(2500);
//                        ActorHandler.shutdownSystem();
//                    } else {
//                        LOGGER.info(numOfRecord + ": [" + c.getId() + "]" + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql());
//                        printCollections(LocalDateTime.now() + " | " + c.getId() + " | " + c.getCost_a() + " | " + c.getCost_q() + " | " + c.getSql() + "\n");
//                    }
                })
                .matchAny(o -> LOGGER.info("Received unknown message"))
                .build();
    }
}
