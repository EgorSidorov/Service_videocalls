package com.rsoi2app.controllers;

import com.rsoi2app.config.Startup;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Utils {
    public static String requestForService(String urlString,String cookie, Services numberService) throws IOException {
        String firstSymbol;
        if(urlString.contains("?"))
            firstSymbol = "&";
        else
            firstSymbol = "?";
        if(numberService == Services.Account)
            urlString += firstSymbol+"serviceToken=" + Startup.getAccountToken() + "&serviceSalt=" + Startup.getAccountSalt() + "&serviceTime=" + Startup.getAccountTime();
        if(numberService == Services.Calls)
            urlString += firstSymbol+"serviceToken=" + Startup.getCallsToken() + "&serviceSalt=" + Startup.getCallsSalt() + "&serviceTime=" + Startup.getCallsTime();
        if(numberService == Services.Payment)
            urlString += firstSymbol+"serviceToken=" + Startup.getPaymentToken() + "&serviceSalt=" + Startup.getPaymentSalt() + "&serviceTime=" + Startup.getPaymentTime();
        if(numberService == Services.WebRTC)
            urlString += firstSymbol+"serviceToken=" + Startup.getWebRTCToken() + "&serviceSalt=" + Startup.getWebRTCSalt() + "&serviceTime=" + Startup.getWebRTCTime();
        InputStream is;
        InputStreamReader reader;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            writeGetInfo(connection,cookie);
            connection.connect();
            is = connection.getInputStream();
            if(connection.getResponseCode() == 405) {
                updateToken(numberService);
                url = new URL(urlString);
                connection = (HttpURLConnection)url.openConnection();
                writeGetInfo(connection, cookie);
                connection.connect();
                is = connection.getInputStream();
            }
            reader = new InputStreamReader(is);
        }
        catch (Exception e)
        {
            return "Error:Timeout"+e.getMessage();
        }
        return getResult(reader);
    }

    public static String easyRequestForService(String urlString)  throws IOException
    {

        InputStream is;
        InputStreamReader reader;
        try {
            URLConnection connection = new URL(urlString).openConnection();
            connection.setReadTimeout(1000);
            writeGetInfo(connection,"none");
            is = connection.getInputStream();
            reader = new InputStreamReader(is);
        }
        catch (Exception e)
        {
            return "Error:"+e.getMessage();
        }
        return getResult(reader);
    }

    public static String requestPostForService(String urlString,String postdata, String cookie, Services numberService) throws IOException {
        String firstSymbol;
        if(urlString.contains("?"))
            firstSymbol = "&";
        else
            firstSymbol = "?";
        if(numberService == Services.Account)
            urlString += firstSymbol+"serviceToken=" + Startup.getAccountToken() + "&serviceSalt=" + Startup.getAccountSalt() + "&serviceTime=" + Startup.getAccountTime();
        if(numberService == Services.Calls)
            urlString += firstSymbol+"serviceToken=" + Startup.getCallsToken() + "&serviceSalt=" + Startup.getCallsSalt() + "&serviceTime=" + Startup.getCallsTime();
        if(numberService == Services.Payment)
            urlString += firstSymbol+"serviceToken=" + Startup.getPaymentToken() + "&serviceSalt=" + Startup.getPaymentSalt() + "&serviceTime=" + Startup.getPaymentTime();
        if(numberService == Services.WebRTC)
            urlString += firstSymbol+"serviceToken=" + Startup.getWebRTCToken() + "&serviceSalt=" + Startup.getWebRTCSalt() + "&serviceTime=" + Startup.getWebRTCTime();
        InputStream is;
        InputStreamReader reader;
        try {
            updateToken(numberService);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            writeGetInfo(connection,cookie);
            connection.addRequestProperty("Content-type", "application/json");
            connection.connect();
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postdata);
            writer.flush();
            is = connection.getInputStream();
            if(connection.getResponseCode() == 405)
            {
                updateToken(numberService);
                url = new URL(urlString);
                connection = (HttpURLConnection)url.openConnection();
                writeGetInfo(connection,cookie);
                connection.addRequestProperty("Content-type", "application/json");
                connection.connect();
                writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(postdata);
                writer.flush();
                is = connection.getInputStream();
            }
            reader = new InputStreamReader(is);
            //new RestTemplate();
        }
        catch (Exception e)
        {
            return "Error:"+e.getMessage();
        }
            String result = getResult(reader);
        return result;
    }

    private static String getResult(InputStreamReader reader) throws IOException {
        char[] buffer = new char[30240];
        int rc;

        StringBuilder sb = new StringBuilder();
        while ((rc = reader.read(buffer)) != -1)
            sb.append(buffer, 0, rc);
        reader.close();
        return sb.toString();
    }

    private static void writeGetInfo(URLConnection connection,String cookie) throws IOException {
        connection.setConnectTimeout(Startup.GetTimeout());
        connection.setReadTimeout(Startup.GetTimeout());
        connection.setDoOutput(true);
        connection.setDoInput(true);
        if(!cookie.contains("none"))
            connection.addRequestProperty("Cookie", "Token=" + cookie);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(Startup.servicePassword);
        writer.flush();
    }

    public static class UserInfo{
        public Boolean getStatus() {
            return Status;
        }

        public void setStatus(Boolean status) {
            Status = status;
        }

        public Boolean getLogged() {
            return IsLogged;
        }

        public void setLogged(Boolean logged) {
            IsLogged = logged;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        private Boolean Status;
        private Boolean IsLogged;
        private String username;
    }

    public static UserInfo GetUserInfo(String token) throws IOException, ParseException {
        UserInfo info = new UserInfo();
        if(token.isEmpty() || token.equals("") || token.contains(" "))
        {
            info.setStatus(false);
            info.setLogged(false);
            info.setUsername("");
            return info;
        }
        String responseStr = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
        JSONParser parser = new JSONParser();
        JSONObject userJson = new JSONObject();
        userJson = (JSONObject) parser.parse(responseStr);
        if (userJson.get("Status").equals("Success"))
            info.setStatus(true);
        else {
            info.setStatus(false);
            info.setLogged(false);
            info.setUsername("");
            return info;
        }
        if (userJson.get("IsLogged").toString().equals("true")) {
            info.setLogged(true);
            info.setUsername(userJson.get("username").toString());
        }
        else {
            info.setLogged(false);
            info.setUsername("");
        }
        return info;
    }

    public static void updateToken(Services numberService) throws IOException, ParseException {
           String responce = "";
           if(numberService == Services.Account)
                responce = easyRequestForService(Startup.GetAccountService()+"/GetServiceToken");
           if(numberService == Services.Calls)
               responce = easyRequestForService(Startup.GetCallsService()+"/GetServiceToken");
           if(numberService == Services.Payment)
               responce = easyRequestForService(Startup.GetPaymentService()+"/GetServiceToken");
           if(numberService == Services.WebRTC)
               responce = easyRequestForService(Startup.GetWebrtcService()+"/GetServiceToken");
           if(responce.contains("Error"))
               return;
           JSONParser parser = new JSONParser();
           JSONObject userJson = (JSONObject) parser.parse(responce);
           if(numberService == Services.Account) {
               Startup.setAccountToken(userJson.getAsString("token"));
               Startup.setAccountSalt(userJson.getAsString("salt"));
               Startup.setAccountTime(userJson.getAsString("time"));
           }
           if(numberService == Services.Calls) {
               Startup.setCallsToken(userJson.getAsString("token"));
               Startup.setCallsSalt(userJson.getAsString("salt"));
               Startup.setCallsTime(userJson.getAsString("time"));
           }
           if(numberService == Services.Payment) {
               Startup.setPaymentToken(userJson.getAsString("token"));
               Startup.setPaymentSalt(userJson.getAsString("salt"));
               Startup.setPaymentTime(userJson.getAsString("time"));
           }
           if(numberService == Services.WebRTC) {
               Startup.setWebRTCToken(userJson.getAsString("token"));
               Startup.setWebRTCSalt(userJson.getAsString("salt"));
               Startup.setWebRTCTime(userJson.getAsString("time"));
           }
       //}
    }
    public static enum Services{
        Account,
        Calls,
        Payment,
        WebRTC
    }

}
