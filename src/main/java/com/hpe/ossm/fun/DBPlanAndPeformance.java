package com.hpe.ossm.fun;

import com.hpe.ossm.alarmGenerator.actor.DBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;

public class DBPlanAndPeformance {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBQuery.class);

    private static HashMap<String , Serializable> getPlan(String url, String user, String pwd, String sql) throws  NullPointerException, SQLException{
        String planSql;
        HashMap<String, Serializable>res=new HashMap<>();
        if(url.contains("h2")){
            planSql="explain analyze "+ sql;
        }else{
            planSql="explain partitions "+ sql;
        }
        try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
            try (Statement stat = conn.createStatement()) {
                try (ResultSet rs = stat.executeQuery(planSql)) {
                    ResultSetMetaData rsm=rs.getMetaData();
                    if(rs.next()){
                        for(int i=1;i<=rsm.getColumnCount();i++){
                            System.out.println(rsm.getColumnName(i) + " : " + ( rs.getObject(i)==null?"":rs.getObject(i).toString()));
                            res.put(rsm.getColumnName(i), rs.getObject(i)==null?"null":rs.getObject(i).toString());
                        }
                    }
                }
            }
        }
        return res;
    }

    private static HashMap<String , Serializable> getPerformance(String url, String user, String pwd, String sql) throws  NullPointerException, SQLException{
        HashMap<String, Serializable>res=new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
            try (Statement stat = conn.createStatement()) {
                long T=0;
                long Max=0;
                long Min=9999999999999l;
                for(int i=0;i<5;i++){
                    long t=System.currentTimeMillis();
                    try (ResultSet rs = stat.executeQuery(sql)) {
                        long t1=System.currentTimeMillis()-t;
                       T=T+t1;
                       if(t1>Max){
                           Max=t1;
                       }
                       if(t1<Min){
                           Min=t1;
                       }
                    }
                }
                res.put("avg",T/5f);
                res.put("max",Max);
                res.put("min",Min);
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {

//        String url="jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_DELAY=-1;MULTI_THREADED=1;LOCK_MODE=3;LOG=0;UNDO_LOG=0";
//        String user="sa";
//        String pwd="";
        String url="jdbc:mariadb://localhost:3306/test";
        String user="root";
        String pwd="root_123";
        String sql="SELECT identifier,sourceIdentifier,event_time,alarmIdentifier,perceived_severity,operation_context,managed_object,alarm_type,additional_text,probable_cause,state,problem_status,sa_total,operator_note,acknowledgement_timestamp,acknowledgement_user_identifier,alarm_origin,clearance_report_flag,creation_timestamp,domain,escalatedAlarm,last_modification_timestamp,original_event_time,original_severity,previous_state,problemOccurrences,problem_information,specific_problems,target_entities,user_text,target,notificationIdentifier,outageFlag,correlGroup,termination_user_identifier,termination_timestamp,closedBy,releaseUserIdentifier,releaseTimestamp,closeUserIdentifier,closeTimestamp,handledUserIdentifier,handleTimestamp,clearanceTimeStamp,uniqueid FROM temip_alarm WHERE  state='Outstanding' and problem_status='Handled' and operation_context='perf_oc1'  order by event_time desc LIMIT 0 , 500";
        getPlan(url,user,pwd,sql);
    }
}
