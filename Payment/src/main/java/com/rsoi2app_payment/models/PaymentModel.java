package com.rsoi2app_payment.models;

import com.rsoi2app_payment.config.Startup;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentModel {
    public Connection connection;
    Boolean dbStatus;
    Boolean queryStatus;
    int sizePage = 10;
    public ResultSet resObj = null;
    Boolean _isTest = false;

    public PaymentModel()
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

    Boolean CreateConnection()
    {
        String db_uri = Startup.GetConnectionStr();
        try {
            Class.forName(Startup.GetDriver());
        } catch (ClassNotFoundException e) {
            System.out.print("\nError find MSSQL driver\n");
            return false;
        }
        try {
            connection = DriverManager.getConnection(db_uri);

        } catch (SQLException e) {
            System.out.print("\nError get connection "+e.getMessage() + "\n");
            return false;
        }
        return true;
    }

    @Override
    public void finalize() throws SQLException {
        connection.close();
    }

    public Boolean WithdrawCash(String cash, String username)
    {
        Statement stmtObj = RequestDB(
                "UPDATE Payment.Pursy SET Cash = Cash - "+cash+" WHERE Username='"+username+"'",false);
        if(!GetQueryStatus())
        {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public String ShowCash(String username)
    {
        queryStatus = true;
        String outputCash = "";
        Statement stmtObj = RequestDB("SELECT Cash FROM Payment.Pursy WHERE Username='"+username+"'",true);
        if(!GetQueryStatus())
        {
            return "";
        }
        try {
            if (resObj.next()) {
                outputCash = resObj.getString("Cash");
            }
        } catch (SQLException e) {
            queryStatus = false;
            return "";
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return outputCash;
    }

    public Boolean CreatePursy(String username)
    {
        queryStatus = true;
        Statement stmtObj = RequestDB("INSERT INTO Payment.Pursy (Cash,Username) VALUES(0,'"+username+"')",false);
        if(!GetQueryStatus())
        {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public Boolean AddCash(String cash, String username, String orderid)
    {
        queryStatus = true;
        if(username.contains(" ") || cash.contains(" ") || orderid.contains(" "))
            return false;
        RequestDB("INSERT INTO Payment.Story VALUES('"+orderid+"')",false);
        if(!GetQueryStatus())
        {
            return false;
        }
        Statement stmtObj = RequestDB("UPDATE Payment.Pursy SET Cash = Cash + "+cash+" WHERE Username='"+username+"'",false);
        if(!GetQueryStatus())
        {
            return false;
        }
        try {
            if(stmtObj != null)
                stmtObj.close();
        } catch (SQLException e) {}
        return true;
    }

    public List<String> GetLogs(int numberPage)
    {
        queryStatus = true;
        List<String> Logs = new ArrayList<>();
        Statement stmtObj = RequestDB("SELECT str FROM Payment.Logger LIMIT "+String.valueOf(sizePage)+" OFFSET "+String.valueOf(numberPage),true);
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
        Statement stmtObj = RequestDB("INSERT INTO Payment.Logger (str) VALUES('"+logString+"')",false);
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
