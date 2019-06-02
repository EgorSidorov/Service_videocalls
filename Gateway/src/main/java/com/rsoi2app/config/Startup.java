package com.rsoi2app.config;

import java.util.Map;

public class Startup
{
        private static String MYSQLCONNECTION = "jdbc:mysql://speakstars.ru:3306?user=_user_&password=_password_&autoReconnect=true&useSSL=false";
        ////////////////////////
        public static String serviceLogin = "account";
        public static String servicePassword = "qwerty";

        public static String secretVK = "t2TDOTCT3YnIRAsSeAif";
    public static String appIDVK = "5919606";
    public static String paypalclient = "AT9lai_PQk1VRnpFqYvarqZUF4L7spREMAwZ00Zbl02KU_X6ekZNpd8GSFm9Z2l22XwQyg-zkC2OvZGI";
    public static String paypalsecret = "EDjF4IDvrQ68981yOChzo74JLJESB8cAICbNZImMP8rQEYE9xTSS1r3aT_OnZuHuP0yRbZfy7ze_omUw";
    public static Integer timelivems = 10000000;

    public static synchronized String getAccountToken() {
        return AccountToken;
    }

    public static synchronized void setAccountToken(String accountToken) {
        AccountToken = accountToken;
    }

    public static synchronized String getAccountSalt() {
        return AccountSalt;
    }

    public static synchronized void setAccountSalt(String accountSalt) {
        AccountSalt = accountSalt;
    }

    public static synchronized String getAccountTime() {
        return AccountTime;
    }

    public static synchronized void setAccountTime(String accountTime) {
        AccountTime = accountTime;
    }

    public static synchronized String getCallsToken() {
        return CallsToken;
    }

    public static synchronized void setCallsToken(String callsToken) {
        CallsToken = callsToken;
    }

    public static synchronized String getCallsSalt() {
        return CallsSalt;
    }

    public static synchronized void setCallsSalt(String callsSalt) {
        CallsSalt = callsSalt;
    }

    public static synchronized String getCallsTime() {
        return CallsTime;
    }

    public static synchronized void setCallsTime(String callsTime) {
        CallsTime = callsTime;
    }

    public static synchronized String getPaymentToken() {
        return PaymentToken;
    }

    public static synchronized void setPaymentToken(String paymentToken) {
        PaymentToken = paymentToken;
    }

    public static synchronized String getPaymentSalt() {
        return PaymentSalt;
    }

    public static synchronized void setPaymentSalt(String paymentSalt) {
        PaymentSalt = paymentSalt;
    }

    public static synchronized String getPaymentTime() {
        return PaymentTime;
    }

    public static synchronized void setPaymentTime(String paymentTime) {
        PaymentTime = paymentTime;
    }

    public static synchronized String getWebRTCToken() {
        return WebRTCToken;
    }

    public static synchronized void setWebRTCToken(String webRTCToken) {
        WebRTCToken = webRTCToken;
    }

    public static synchronized String getWebRTCSalt() {
        return WebRTCSalt;
    }

    public static synchronized void setWebRTCSalt(String webRTCSalt) {
        WebRTCSalt = webRTCSalt;
    }

    public static synchronized String getWebRTCTime() {
        return WebRTCTime;
    }

    public static synchronized void setWebRTCTime(String webRTCTime) {
        WebRTCTime = webRTCTime;
    }

    public static String AccountToken = "1";
        public static String AccountSalt = "1";
        public static String AccountTime = "1";

        public static String CallsToken = "1";
        public static String CallsSalt = "1";
        public static String CallsTime = "1";

        public static String PaymentToken = "1";
        public static String PaymentSalt = "1";
        public static String PaymentTime = "1";

        public static String WebRTCToken = "1";
        public static String WebRTCSalt = "1";
        public static String WebRTCTime = "1";
        ////////////////////////
        private static String MYSQLDRIVER = "com.mysql.cj.jdbc.Driver";
        
        public static String GetConnectionStr() {
            return MYSQLCONNECTION;
        }

        public static String GetDriver() {
            return MYSQLDRIVER;
        }

        public static int GetTimeout() {
        return 5000;
    }

        public static String GetGatewayHostPort() {return "http://speakstars.ru:8080";}

        public static String GetAccountService() {return GetGatewayHostPort()+"/service/account";}

        public static String GetWebrtcService() {return GetGatewayHostPort()+"/service/webrtc";}

        public static String GetCallsService() {return GetGatewayHostPort()+"/service/calls";}

        public static String GetPaymentService() {return GetGatewayHostPort()+"/service/payment";}
}
