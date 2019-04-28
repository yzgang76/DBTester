package com.hpe.ossm.alarmGenerator;


import com.hpe.ossm.alarmGenerator.actor.DBWorkerManager;
import com.typesafe.config.Config;


import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlarmGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmGenerator.class);

    public static void main(String[] args) {
        try {

            final ActorSystem actorSystem = ActorHandler.getInstance().getActorSystem();
            final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
            final String url = configurations.getString("url");
            final String user = configurations.getString("user");
            final String pwd = configurations.getString("pwd");
            final int ocNum = configurations.getInt("numOfOC");
            final int alarmNum = configurations.getInt("numOfAlarmPerOC");
            final String tableName = configurations.getString("tableName");

            //create table if not exists
            final String sql = "CREATE  TABLE if not exists\n" +
                    "    " + tableName + "\n" +
                    "    (\n" +
                    "        _KEY VARCHAR(255) NOT NULL,\n" +
                    "        _TIMESTAMP BIGINT,\n" +
                    "        ACKNOWLEDGEMENT_TIMESTAMP BIGINT,\n" +
                    "        ACKNOWLEDGEMENT_USER_IDENTIFIER VARCHAR(32),\n" +
                    "        ADDITIONAL_TEXT VARCHAR(4096),\n" +
                    "        ALARM_ORIGIN VARCHAR(255),\n" +
                    "        ALARM_TYPE VARCHAR(255),\n" +
                    "        CHILDREN VARCHAR(255),\n" +
                    "        CLEARANCE_REPORT_FLAG BOOLEAN,\n" +
                    "        CREATION_TIMESTAMP BIGINT,\n" +
                    "        DOMAIN VARCHAR(255),\n" +
                    "        ESCALATEDALARM BOOLEAN,\n" +
                    "        EVENT_TIME BIGINT,\n" +
                    "        IDENTIFIER VARCHAR(255),\n" +
                    "        ALARMIDENTIFIER BIGINT,\n" +
                    "        SOURCEIDENTIFIER VARCHAR(255),\n" +
                    "        OPERATOR_NOTE VARCHAR(4096),\n" +
                    "        LAST_MODIFICATION_TIMESTAMP BIGINT,\n" +
                    "        MANAGED_OBJECT VARCHAR(255),\n" +
                    "        OPERATION_CONTEXT VARCHAR(255),\n" +
                    "        ORIGINAL_EVENT_TIME BIGINT,\n" +
                    "        ORIGINAL_SEVERITY VARCHAR(32),\n" +
                    "        PARENTS VARCHAR(255),\n" +
                    "        PERCEIVED_SEVERITY VARCHAR(32),\n" +
                    "        PREVIOUS_STATE VARCHAR(32),\n" +
                    "        PROBABLE_CAUSE VARCHAR(255),\n" +
                    "        PROBLEMOCCURRENCES BIGINT,\n" +
                    "        PROBLEM_INFORMATION VARCHAR(255),\n" +
                    "        PROBLEM_STATUS VARCHAR(64),\n" +
                    "        SA_TOTAL BIGINT,\n" +
                    "        SPECIFIC_PROBLEMS VARCHAR(255),\n" +
                    "        STATE VARCHAR(32),\n" +
                    "        TARGET_ENTITIES VARCHAR(255),\n" +
                    "        TARGET VARCHAR(255),\n" +
                    "        USER_TEXT VARCHAR(255),\n" +
                    "        UNIQUEID VARCHAR(255),\n" +
                    "        MAPASSOCIATIONID VARCHAR(255),\n" +
                    "        NOTIFICATIONIDENTIFIER VARCHAR(255),\n" +
                    "        OUTAGEFLAG BOOLEAN,\n" +
                    "        TERMINATION_USER_IDENTIFIER VARCHAR(32),\n" +
                    "        TERMINATION_TIMESTAMP BIGINT,\n" +
                    "        CLOSEDBY VARCHAR(255),\n" +
                    "        RELEASEUSERIDENTIFIER VARCHAR(32),\n" +
                    "        RELEASETIMESTAMP BIGINT,\n" +
                    "        CLOSEUSERIDENTIFIER VARCHAR(32),\n" +
                    "        CLOSETIMESTAMP BIGINT,\n" +
                    "        HANDLEDUSERIDENTIFIER VARCHAR(32),\n" +
                    "        HANDLETIMESTAMP BIGINT,\n" +
                    "        CLEARANCETIMESTAMP BIGINT,\n" +
                    "        CORRELTAG VARCHAR(255),\n" +
                    "        CORRELGROUP VARCHAR(255),\n" +
                    "        CORRELNBCLEAREDALARMS VARCHAR(255),\n" +
                    "        CORRELTOTALALARMS VARCHAR(255),\n" +
                    "        CORRELNBACKALARMS VARCHAR(255),\n" +
                    "        CORRELNBOUTSTANDINGALARMS VARCHAR(255),\n" +
                    "        CORRELACTIONLIST VARCHAR(255),\n" +
                    "        CORRELACTIONRESULT VARCHAR(255),\n" +
                    "        CORRELKEY VARCHAR(255),\n" +
                    "        CORRELNODEID VARCHAR(255),\n" +
                    "        SITELOCATION VARCHAR(255),\n" +
                    "        REGIONLOCATION VARCHAR(255),\n" +
                    "        VENDORNAME VARCHAR(255),\n" +
                    "        TECHNOLOGYDOMAIN VARCHAR(255),\n" +
                    "        PRIMARY KEY (_KEY)\n" +
                    "    );\n";
            System.out.println(url + "|" + ocNum + "|" + alarmNum);
            try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
                LOGGER.info("try to create table");
                try (Statement stat = conn.createStatement()) {
                    try {
                        stat.execute(sql);
                        stat.execute("truncate table "+tableName);
                        stat.execute("Create index if not exists severity_index on temip_alarm(perceived_severity)");
                        stat.execute("Create index if not exists event_time_index on temip_alarm(event_time)");
                        stat.execute("Create index if not exists  alarm_identifier_index on temip_alarm(alarmidentifier)");
                        stat.execute("Create index if not exists  alarm_state_index on temip_alarm(`state`)");
                        stat.execute("Create index  if not exists alarm_problem_status_index on temip_alarm (problem_status)");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }

            //start worker manager actor
            actorSystem.actorOf(DBWorkerManager.props());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            LOGGER.error("Error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
