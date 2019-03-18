package com.hpe.ossm.mariaDB.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class prepareData {
    private void createTable() throws Exception{
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://16.17.100.97:3306/test", "root", "root_123")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                final String sql="CREATE OR REPLACE TABLE `temip_alarm`  (\n" +
                        "\t`ts` BIGINT(20) NULL DEFAULT NULL,\n" +
                        "\t`id` VARBINARY(50) NOT NULL,\n" +
                        "\t`severity` VARCHAR(50) NULL DEFAULT NULL,\n" +
                        "\t`domain` VARCHAR(50) NULL DEFAULT NULL,\n" +
                        "\t`oc` VARCHAR(50) NULL DEFAULT NULL,\n" +
                        "\tPRIMARY KEY (`id`),\n" +
                        "\tINDEX `oc` (`oc`)\n" +
                        ")\n" +
                        "ENGINE=InnoDB\n" +
                        ";";
                stmt.execute(sql);
                stmt.execute("TRUNCATE `temip_alarm`;");
            }
        }
    }

    private void insertData(int num) throws Exception{
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/test", "root", "root")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                for(int i=0;i<num;i++){
                    final String sql="insert into temip_alarm (ts,id,severity,domain,oc) values("+ System.currentTimeMillis()+",'oc1_"+i+"','warning','domain1','oc1');";
                    System.out.println(sql);
                    stmt.execute(sql);
                }

            }
        }
    }

    private void queryData() throws Exception{
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/test", "root", "root")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
               try(ResultSet rs=stmt.executeQuery("select * from temip_alarm")){
                   rs.first();
                   System.out.println(rs.getBigDecimal(1));
               }
            }
        }
    }


    public static void main(String [] args)throws Exception{
        prepareData p=new prepareData();
        p.createTable();
        p.insertData(10);
        p.queryData();
        System.out.println(LocalDateTime.now());

//        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://16.17.100.97:3306/test?characterEncoding=utf-8", "root", "root_123")) {
//            try (Statement stmt = conn.createStatement()) {
//                //execute query
//                try (ResultSet rs = stmt.executeQuery("SELECT * FROM TEMIP_ALARM")) {
//                    //position result to first
//                    rs.first();
//                    System.out.println(rs.getString(1));
//                }
//            }
//        }
    }
}
