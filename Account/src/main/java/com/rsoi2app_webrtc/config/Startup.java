package com.rsoi2app_webrtc.config;

import java.util.HashMap;
import java.util.Map;

public class Startup
{
        private static String MYSQLCONNECTION = "";
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
