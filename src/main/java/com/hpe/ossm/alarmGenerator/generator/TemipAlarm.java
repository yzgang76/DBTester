package com.hpe.ossm.alarmGenerator.generator;


import com.hpe.ossm.alarmGenerator.ActorHandler;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemipAlarm {
    private int alarmID=0;
    private static final Logger LOGGER = LoggerFactory.getLogger(TemipAlarm.class);
//    private final ActorSystem actorSystem = ActorHandler.getInstance().getActorSystem();
    private final Config configurations = ActorHandler.getInstance().getConfig().getConfig("configurations");
    private final String tableName=configurations.getString("tableName");
    private final String sqlHeaders = "insert into "+tableName+" (_KEY," +
            "_TIMESTAMP," +
            "ACKNOWLEDGEMENT_TIMESTAMP," +
            "ACKNOWLEDGEMENT_USER_IDENTIFIER," +
            "ADDITIONAL_TEXT," +
            "ALARM_ORIGIN," +
            "ALARM_TYPE," +
//            "CHILDREN," +
            "CLEARANCE_REPORT_FLAG," +
            "CREATION_TIMESTAMP," +
            "DOMAIN," +
            "ESCALATEDALARM," +
            "EVENT_TIME," +
            "IDENTIFIER," +
            "ALARMIDENTIFIER," +
            "SOURCEIDENTIFIER," +
            "OPERATOR_NOTE," +
            "LAST_MODIFICATION_TIMESTAMP," +
            "MANAGED_OBJECT," +
            "OPERATION_CONTEXT," +
            "ORIGINAL_EVENT_TIME," +
            "ORIGINAL_SEVERITY," +
//            "PARENTS," +
            "PERCEIVED_SEVERITY," +
            "PREVIOUS_STATE," +
            "PROBABLE_CAUSE," +
            "PROBLEMOCCURRENCES," +
//            "PROBLEM_INFORMATION," +
            "PROBLEM_STATUS," +
            "SA_TOTAL," +
//            "SPECIFIC_PROBLEMS," +
            "STATE," +
//            "TARGET_ENTITIES," +
            "TARGET," +
//            "USER_TEXT," +
//            "UNIQUEID," +
//            "MAPASSOCIATIONID," +
//            "NOTIFICATIONIDENTIFIER," +
//            "OUTAGEFLAG," +
            "TERMINATION_USER_IDENTIFIER," +
            "TERMINATION_TIMESTAMP" +
//            "CLOSEDBY," +
//            "RELEASEUSERIDENTIFIER," +
//            "RELEASETIMESTAMP," +
//            "CLOSEUSERIDENTIFIER," +
//            "CLOSETIMESTAMP," +
//            "HANDLEDUSERIDENTIFIER," +
//            "HANDLETIMESTAMP," +
//            "CLEARANCETIMESTAMP," +
//            "CORRELTAG," +
//            "CORRELGROUP," +
//            "CORRELNBCLEAREDALARMS," +
//            "CORRELTOTALALARMS," +
//            "CORRELNBACKALARMS," +
//            "CORRELNBOUTSTANDINGALARMS," +
//            "CORRELACTIONLIST," +
//            "CORRELACTIONRESULT," +
//            "CORRELKEY," +
//            "CORRELNODEID," +
//            "SITELOCATION," +
//            "REGIONLOCATION," +
//            "VENDORNAME"+
            ")";

    private int getRandom() {
        return (int) Math.round(Math.random() * 100);
    }

    private String getMANAGED_OBJECT() {
        int r = getRandom();
        if (r < 25) {
            return "osi_system domain1_system testobj t1";
        }
        if (r < 50) {
            return "osi_system domain1_system testobj t2";
        }
        if (r < 75) {
            return "osi_system domain2_system testobj t1";
        }
        return "osi_system domain2_system testobj t2";
    }

    private String getORIGINAL_SEVERITY() {
        int r = getRandom();
        if (r < 10) {
            return "Minor";
        }
        if (r < 20) {
            return "Indeterminate";
        }
        if (r < 40) {
            return "Warning";
        }
        if (r < 70) {
            return "Minor";
        }
        return "Critical";
    }
    private String getProblemStatus(){
        int r = getRandom();
        if (r<33){
            return "Closed";
        }
        if(r<66) {
            return "Handled";
        }
        return "Not-Handled";
    }
    private String getPROBABLE_CAUSE() {
        int r = getRandom();
        if (r < 10) {
            return "Fire";
        }
        if (r < 20) {
            return "Fire";
        }
        if (r < 40) {
            return "Fire";
        }
        if (r < 70) {
            return "ResponseTimeExcessive";
        }
        return "EquipmentMalfunction";
    }

    private String getPREVIOUS_STATE() {
        int r = getRandom();
        if (r < 85) {
            return "Outstanding";
        }
        return "Acknowledged";
    }
    private String getSTATE() {
        int r = getRandom();
        if (r < 15) {
            return "Acknowledged";
        }
        if (r < 30) {
            return "Terminated";
        }
        return "Outstanding";
    }
    private static String getRandomString(int length){
        String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        java.util.Random random=new java.util.Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0; i<length; ++i){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    public String getAlarmSql(String oc) {
        final long ts = System.currentTimeMillis()-100000000L;
//        final String oc = "demo_oc1";
        final int alarmIdentifier =alarmID++;
        String ACKNOWLEDGEMENT_USER_IDENTIFIER ;
        final String ALARM_ORIGIN = "IncomingAlarm";
        final String ALARM_TYPE = "COMMUNICATIONS_ALARM";
        final Boolean CLEARANCE_REPORT_FLAG = false;
        final Boolean ESCALATEDALARM = false;
        final String domain = getRandom() > 50 ? "ossv195_ns:.domain2" : "ossv195_ns:.domain1";
        final String identifier = "operation_context " + oc + " alarm_object " + alarmIdentifier;
        final String SOURCEIDENTIFIER = "AnyTeMIP##TeMIP_195##TeMIPDynamicFlow";
        final String OPERATOR_NOTE = "OPERATOR_NOTE";
        final String MANAGED_OBJECT = getMANAGED_OBJECT();
        final String ORIGINAL_SEVERITY = getORIGINAL_SEVERITY();
        final String PERCEIVED_SEVERITY = ORIGINAL_SEVERITY;
        final String PREVIOUS_STATE = getPREVIOUS_STATE();
        final String PROBABLE_CAUSE = getPROBABLE_CAUSE();
        final String PROBLEM_STATUS = getProblemStatus();
        final String TARGET = "TEMIP";
        final String STATE = getSTATE();
        String TERMINATION_USER_IDENTIFIER;
        Long ACKNOWLEDGEMENT_TIMESTAMP;
        Long TERMINATION_TIMESTAMP;
        if("Outstanding".equals(STATE)){
            ACKNOWLEDGEMENT_USER_IDENTIFIER="";
            ACKNOWLEDGEMENT_TIMESTAMP=null;
            TERMINATION_USER_IDENTIFIER="";
            TERMINATION_TIMESTAMP=null;
        }else if("Acknowledged".equals(STATE)){
            ACKNOWLEDGEMENT_USER_IDENTIFIER="admin";
            ACKNOWLEDGEMENT_TIMESTAMP=System.currentTimeMillis();
            TERMINATION_USER_IDENTIFIER="";
            TERMINATION_TIMESTAMP=null;
        }else{
            ACKNOWLEDGEMENT_USER_IDENTIFIER="";
            ACKNOWLEDGEMENT_TIMESTAMP=null;
            TERMINATION_USER_IDENTIFIER="admin";
            TERMINATION_TIMESTAMP=System.currentTimeMillis();
        }

        final int PROBLEMOCCURRENCES = 1;
        final int SA_TOTAL = 0;
        final String ADDITIONAL_TEXT = "N-URG, , RADIO X-CEIVER ADMINISTRATION BTS EXTERNAL FAULT, , cfg cmdtxt logs perf_alarms_bycreate.sh perf_alarms_bydisable.sh send_alarm_bycreate.sh send_alarm_bydisable.sh start_nohup_bycreate.sh start_nohup_bydisable.sh ALARM 266 A2/EXT COI4BE_16B/01/0''U 171012 1532, RADIO X-CEIVER ADMINISTRATION, BTS EXTERNAL FAULT, , MO RSITE CLASS, RXOCF-211 BURINHOSA 2, , EXTERNAL ALARM, JB3527 ASCOM NON URGENT ALARM, , END, -ProbableCause(OSS)=Different causes possible for same message, _CORR_FIL_TAG_, , MANAGED OBJECT =, SubNetwork=ONRM_ROOT_MO,SubNetwork=GSM,ManagedElement=COI4BE,BssFunction=BSS_ManagedFuncti, on,BtsSiteMgr=BURINHOSA, , GSM_NEW_ALF, CD3527, IP-Address: 10.136.190.196, ################### APPENDED EAS ADDITIONAL TEXT###################, [customFields, domain = "+domain+",creationTime = 2017-10-12T14:32:51.454Z, creationTimestamp = "+ts+", notificationIdentifier = "+alarmIdentifier+", originalSeverity = "+ORIGINAL_SEVERITY+ ", ], ************FREDG**************, [originatingManagedEntityStructure, OSS-RC = coi4be, N =, E = cd3527, ], [attributeChanges, ], [specificProblem = [ [ 1 ], [ OSS-RC, N, E], Specific Problem, Communications Alarm ]], [probableCause = Fire], [networkState = NOT_CLEARED], [problemState = NOT_HANDLED], [identifier = "+identifier+"], [target = ptRAN.ran2g_flow], , , <ALM_ID>"+identifier+"<ALM_ID>, , Enriched by ALB: PT |CD3527|<ALM_ID>operation_context perf_oc1 alarm_object, 9708529<ALM_ID> CROSS-DOMAIN 9708529 "+getRandomString(548);
        final String sql = sqlHeaders + " values ('" + identifier + "',"
                + ts + ","
                + ACKNOWLEDGEMENT_TIMESTAMP + ",'"
                + ACKNOWLEDGEMENT_USER_IDENTIFIER + "','"
                + ADDITIONAL_TEXT + "','"
                + ALARM_ORIGIN + "','"
                + ALARM_TYPE + "',"
                + CLEARANCE_REPORT_FLAG + ","
                + ts + ",'"
                + domain + "',"
                + ESCALATEDALARM + ","
                + ts + ",'"
                + identifier + "','"
                + alarmIdentifier + "','"
                + SOURCEIDENTIFIER + "','"
                + OPERATOR_NOTE + "',"
                + ts + ",'"
                + MANAGED_OBJECT + "','"
                + oc + "',"
                + ts + ",'"
                + ORIGINAL_SEVERITY + "','"
                + PERCEIVED_SEVERITY + "','"
                + PREVIOUS_STATE + "','"
                + PROBABLE_CAUSE + "',"
                + PROBLEMOCCURRENCES + ",'"
                + PROBLEM_STATUS + "',"
                + SA_TOTAL + ",'"
                + STATE + "','"
                + TARGET + "','"
                + TERMINATION_USER_IDENTIFIER + "',"
                + TERMINATION_TIMESTAMP
                + ")";
//        System.out.println(sql);
        return sql;
    }

    public static void main(String[] args) {
        TemipAlarm t = new TemipAlarm();
        t.getAlarmSql("demo_oc1");
    }
}
