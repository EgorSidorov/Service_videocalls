package com.rsoi2app_webrtc.models;

import com.rsoi2app_webrtc.config.Startup;
import com.rsoi2app_webrtc.external.EasyAuth;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountModel {
    public Connection connection;
    private Boolean dbStatus;
    private Boolean queryStatus;
    public EasyAuth auth;
    public ResultSet resObj;
    int sizePage = 2;

    public AccountModel()
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
        auth = new EasyAuth(db_uri, "Account.Info", "Username", "Password", "Cookie");
        return true;
    }

    @Override
    public void finalize() throws SQLException {
        connection.close();
        auth.finalize();
    }

    public List<String> GetUserNames(int numberPage)
    {
        List<String> UserNames = new ArrayList<>();
            Statement stmtObj = RequestDB(
                    "SELECT Username " +
                            "FROM Account.Info us " +
                            //"JOIN Account.Roles rl ON(us.Role=rl.ID_Role) " +
                            "LIMIT " + String.valueOf(sizePage) + " OFFSET " + String.valueOf(numberPage * sizePage), true);
            if (!GetQueryStatus())
                return UserNames;
            try {
                while (resObj.next()) {
                    UserNames.add(resObj.getString("Username")); //+ ":" + resObj.getString("Name_Role"));
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

    public List<String> GetAllRoles()
    {
        List<String> Roles = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT Name_Role FROM Account.Roles", true);
        if (!GetQueryStatus())
            return Roles;
        try {
            while (resObj.next()) {
                Roles.add(resObj.getString("Name_Role"));
            }
        } catch (SQLException e) {
            queryStatus = false;
            return Roles;
        }
        try {
            if (stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {
        }
        return Roles;
    }

    public Boolean CreateUser(String username, String password,String role)
    {
        Boolean success = auth.CreateAccount(username, password);
        return success;
    }

    public Boolean DeleteUser(String username, String password,String role)
    {
        Boolean success = auth.DeleteAccount(username, password);
        return success;
    }

    public Boolean AddRole(int ID, String role)
    {
        return SetWithoutAnswer("Insert INTO Account.Roles VALUES(" + Integer.toString(ID) + ",'" + role + "')");
    }

    public String Login(String username, String password)
    {
        queryStatus = true;
        String authCookie = auth.LogIn(username, password);
        if (auth.GetQueryStatus())
            return authCookie;
        else {
            queryStatus = false;
            return "";
        }
    }

    public Boolean Logout(String token)
    {
        if(auth.HasLogged(token))
            return auth.LogOut(token);
        else return false;
    }

    public Boolean IsLogged(String token)
    {
        return auth.HasLogged(token);
    }

    public String GetUsername(String token)
    {
        queryStatus = true;
        String username = auth.GetUserName(token);
        if (auth.GetQueryStatus()) {
            return username;
        } else {
            queryStatus = false;
            return "";
        }
    }

    public String GetRole(String token)
    {
        String role = GetStringFromTable("Name_Role","Account.Roles rl"," JOIN Account.Info inf ON(inf.Role = rl.ID_Role) WHERE inf.Cookie=" + token);
        return role;
    }
    
    public List<String> GetLogs(int numberPage)
    {
        queryStatus = true;
        List<String> Logs = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT str FROM Account.Logger LIMIT "+String.valueOf(sizePage)+" OFFSET "+String.valueOf(numberPage),true);
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

    public Boolean SetAuthToken(String username, String appID, String authtoken)
    {
        return SetWithoutAnswer("INSERT INTO Account.Oauth2 (appId,Username,authtoken) " +
                "values("+appID+",'"+username+"','"+authtoken+"') " +
                "ON DUPLICATE KEY UPDATE authtoken='"+authtoken+"'");
    }

    public String getRedirectURI(String appID)
    {
        return GetStringFromTable("redirectURI","Account.OauthApps"," WHERE appId="+appID);
    }
    public String getScope(String appID)
    {
        return GetStringFromTable("scope","Account.OauthApps"," WHERE appId="+appID);
    }


    public Boolean checkAuthToken(String username, String appID, String authtoken, String secret)
    {
        return !GetStringFromTable("oa.appId","Account.Oauth2"," oa JOIN Account.OauthApps oaa ON oa.appId = oaa.appId WHERE username='"+username+"' AND oa.appId="+appID+" AND authtoken='"+authtoken+"' AND oaa.secret='"+secret+"'").equals("emptystr");
    }

    public Boolean checkAccessToken(String username, String appID, String accesstoken)
    {
        return !GetStringFromTable("appId","Account.Oauth2"," WHERE username='"+username+"' AND appId="+appID+" AND accesstoken='"+accesstoken+"'").equals("emptystr");
    }

    public Boolean checkRefreshToken(String username, String appID, String refreshtoken, String secret)
    {
        return !GetStringFromTable("oa.appId","Account.Oauth2"," oa JOIN Account.OauthApps oaa ON oa.appId = oaa.appId WHERE username='"+username+
                "' AND oa.appId="+appID+" AND refreshtoken='"+refreshtoken+"' AND oaa.secret='"+secret+"'").equals("emptystr");
    }

    public Boolean SetAccessToken(String username, String appID, String accesstoken, String refreshtoken)
    {
        String time = String.valueOf(System.currentTimeMillis());
        return SetWithoutAnswer("UPDATE Account.Oauth2 SET time="+time+",accesstoken='"+accesstoken+"',refreshtoken='"+refreshtoken+"',authtoken='' "+
                "WHERE appID="+appID+" AND username='"+username+"'");
    }
    
   public Boolean SetLogs(String logString)
    {
        return SetWithoutAnswer("INSERT INTO Account.Logger (str) VALUES('"+logString+"')");
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

    public Boolean SetWithoutAnswer(String sql)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB(sql,false);
        if(!GetQueryStatus()) {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
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
            if(resObj.next())
                result = resObj.getString(namefield);
            else result = "emptystr";
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
