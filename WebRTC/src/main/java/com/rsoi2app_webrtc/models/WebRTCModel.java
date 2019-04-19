package com.rsoi2app_webrtc.models;

import com.rsoi2app_webrtc.config.Startup;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WebRTCModel {
    public Connection connection;
    private Boolean dbStatus;
    private Boolean queryStatus;
    public ResultSet resObj;
    int sizePage = 2;

    public WebRTCModel()
    {
        dbStatus = CreateConnection();
    }

    public Boolean GetDbStatus()
    {
        return  dbStatus;
    }

    public Boolean GetQueryStatus()
    {
        return  queryStatus;
    }


    public Boolean CreateConnection()
    {
        String db_uri = Startup.GetConnectionStr();
        try {
            Class.forName(Startup.GetDriver());
        } catch (ClassNotFoundException e) {
            System.out.print("\nError find sql driver\n");
            return false;
        }
        try {
            connection = DriverManager.getConnection(db_uri);
        } catch (SQLException e) {
            System.out.print("\nError get connection " + e.getMessage() + "\n");
            return false;
        }
        return true;
    }

    @Override
    public void finalize() throws SQLException {
        connection.close();
    }

    public List<String> GetPhonebookNames(String usernamefrom)
    {
        List<String> UserNames = new ArrayList<>();
            Statement stmtObj = RequestDB(
                    "SELECT Username,  Usernameto " +
                            "FROM Speakstars.phonebook pb WHERE Username='"+usernamefrom+"'",true);
            if (!GetQueryStatus())
                return UserNames;
            try {
                while (resObj.next()) {
                    if(!resObj.getString("Usernameto").equals(usernamefrom))
                        UserNames.add(resObj.getString("Usernameto"));// + ":" + resObj.getString("Name_Role"));
                }
            } catch (SQLException e) {
                queryStatus = false;
                return UserNames;
            }
            try {
                if (stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e) {
            }
        return UserNames;
    }

    public Boolean SetCallRequest(String fromuser, String touser, Boolean first_time, String countfromicecandidates)
    {
        queryStatus = true;
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj;
        if(first_time)
            stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET touser='"+touser+"',  time="+time+", status=0, countfromicecandidates="+countfromicecandidates+" WHERE fromuser='"+fromuser+"'",false);
        else
            stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET touser='"+touser+"',  time="+time+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestFromIceCandidates(String fromuser, String icecandidates)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET fromicecandidates='"+icecandidates+"' WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean AddPhoneBook(String user, String touser)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("INSERT INTO Speakstars.phonebook select * from (select '"+user+"','"+touser+"') as tmp where not exists (select * from Speakstars.phonebook where username='"+user+"' and usernameto='"+touser+"')",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestToIceCandidates(String fromuser, String icecandidates )
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET toicecandidates='"+icecandidates+"' WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestFromDescription(String fromuser, String description)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET fromdescription="+description+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallRequestToDescription(String fromuser,String description)
    {
        queryStatus = true;
        Statement stmtObj;
        stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET todescription="+description+" WHERE fromuser='"+fromuser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean SetCallAnswer(String touser, String fromuser, String status, String counttoicecandidates)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB("UPDATE Speakstars.CallRequest SET status="+status+", counttoicecandidates="+counttoicecandidates+" WHERE fromuser='"+fromuser+"' AND touser='"+touser+"'",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public List<String> GetCallRequest(String username)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT fromuser FROM Speakstars.CallRequest WHERE touser='"+username+"' and ("+time+"-time)<5000 and status=0",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromuser"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetCallAnswer(String fromuser, String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT status FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("status"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        finally {
            try {
                if(stmtObj != null)
                    stmtObj.close();
            } catch (SQLException e){}
        }
        return users;
    }

    public String GetStringFromTable (String namefield, String nametable, String condition)
    {
        queryStatus = true;
        String result = "";
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT " + namefield  + " FROM "+nametable +" "+condition,true);
        if(!GetQueryStatus()) {
            return result;
        }
        try {
            resObj.next();
            result = resObj.getString(namefield);
        } catch (SQLException e) {
            queryStatus = false;
            return result;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return result;
    }

    public List<String> GetFromIceCandidates(String touser , String fromuser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT fromicecandidates FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromicecandidates"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetToIceCandidates(String fromuser , String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT toicecandidates FROM Speakstars.CallRequest where fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("toicecandidates"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetFromDescription(String touser , String fromuser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT fromdescription FROM Speakstars.CallRequest WHERE touser='"+touser+"' and fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("fromdescription"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    public List<String> GetToDescription(String fromuser, String touser)
    {
        queryStatus = true;
        List<String> users = new ArrayList<>();
        String time = String.valueOf(System.currentTimeMillis());
        Statement stmtObj = RequestDB("SELECT todescription FROM Speakstars.CallRequest WHERE fromuser='"+fromuser+"'",true);
        if(!GetQueryStatus()) {
            return users;
        }
        try {
            while(resObj.next()) {
                users.add(resObj.getString("todescription"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return users;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return users;
    }

    
    public List<String> GetLogs(int numberPage)
    {
        queryStatus = true;
        List<String> Logs = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT str FROM Speakstars.Logger LIMIT "+String.valueOf(sizePage)+" OFFSET "+String.valueOf(numberPage),true);
        if(!GetQueryStatus()) {
            return Logs;
        }
        try {
            while(resObj.next()) {
                Logs.add(resObj.getString("str"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return Logs;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return Logs;
    }
    
   public Boolean SetLogs(String logString)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB("INSERT INTO Speakstars.Logger (str) VALUES('"+logString+"')",false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean checkServiceToken(String serviceToken, String salt, String time)
    {
        long currentTime = System.currentTimeMillis();
        long inputTime = Long.parseLong(time);
        SetLogs("checkServiceToken salt="+salt+" time="+time+" serviceToken="+serviceToken);
        if(DigestUtils.md5Hex(Startup.serviceLogin+Startup.servicePassword+salt+time).equals(serviceToken) &&
                currentTime < inputTime){
            return false;
        } else return true;
    }

    public Statement RequestDB(String sql, Boolean response)
    {
        queryStatus = true;
        if(!GetDbStatus())
        {
            queryStatus = false;
            return null;
        }
        Statement stmtObj = null;
        try {
            stmtObj = connection.createStatement();
        } catch (SQLException e) {
            queryStatus = false;
            return null;
        }
        try {
            if(response)
                resObj = stmtObj.executeQuery(sql);
            else
                stmtObj.execute(sql);
        } catch (SQLException e) {
            queryStatus = false;
        }
        finally {
            return stmtObj;
        }
    }
}
