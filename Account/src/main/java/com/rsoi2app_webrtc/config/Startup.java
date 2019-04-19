package com.rsoi2app_webrtc.config;

import java.util.HashMap;
import java.util.Map;

public class Startup
{
        private static String MYSQLCONNECTION = "jdbc:mysql://speakstars.ru:3306?user=mirton&password=VDBE090968&autoReconnect=true&useSSL=false";
        public static String serviceLogin = "account";
        public static String servicePassword = "qwerty";
        public static Integer timelivems = 10000000;
        private static String MYSQLDRIVER = "com.mysql.cj.jdbc.Driver";
        
        public static String GetConnectionStr() {
            return MYSQLCONNECTION;
        }

        public static String GetDriver() {
            return MYSQLDRIVER;
        }
}
