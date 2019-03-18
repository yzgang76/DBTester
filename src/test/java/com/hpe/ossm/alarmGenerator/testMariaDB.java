package com.hpe.ossm.alarmGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;

public class testMariaDB {
    @BeforeClass
    public static void setup() {
    }
    @Test
    public final void test() throws Exception{
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://16.17.100.97:3306/test", "root", "root_123")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
//                try (ResultSet rs = stmt.executeQuery("SELECT * FROM temip_alarm")) {
//                    //position result to first
//                    rs.first();
//                    System.out.println(rs.getString(1));
//                }
            }
        }
    }
}
