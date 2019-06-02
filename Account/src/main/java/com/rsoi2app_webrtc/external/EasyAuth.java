package com.rsoi2app_webrtc.external;

import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.Random;

public class EasyAuth {
    private Connection _connection;
    private Boolean _queryStatus = false;
    private Boolean _dbStatus;
    private String  _stringConnection;
    public String  _tableName;
    public String  _nameFieldUserName;
    public String  _nameFieldPassword;
    private String  _nameFieldToken;
    private String  _nameFieldTime;
    private int     _timeLiveTokenms = 60000*300;
    private int     _countAviableTokens;
    private String  _errorMessage = "";
    private String  _lastRequest = "";
    private ResultSet resObj;

    public EasyAuth(String stringConnection, String tableName, String nameFieldUserName, String nameFieldPassword, String nameFieldToken)
    {
        _stringConnection = stringConnection;
        _tableName = tableName;
        _nameFieldUserName = nameFieldUserName;
        _nameFieldPassword = nameFieldPassword;
        _nameFieldToken = nameFieldToken;
        _nameFieldTime = "time";
        _dbStatus = CreateConnection();
    }

    @Override
    public void finalize() throws SQLException {
        _connection.close();
    }

    public Boolean GetDbStatus()
    {
        return _dbStatus;
    }

    public String GetLastRequest()
    {
        return _lastRequest;
    }

    public Boolean GetQueryStatus()
    {
        return _queryStatus;
    }

    public String GetLastErrorMessage()
    {
        return _errorMessage;
    }

    public Boolean CreateAccount(String userName, String password)
    {
        if (!CheckUsernamePassword(userName, password))
            return false;
        password = DigestUtils.md5Hex(password);
        Statement stmtObj = RequestDB("SELECT * FROM " + _tableName + " WHERE " + _nameFieldUserName + "='" + userName + "'", true);
        if (!GetQueryStatus())
            return false;
        Boolean hasNext = false;
        try {
            hasNext = resObj.next();
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
            return false;
        } finally {
            if (stmtObj != null) {
                try {
                    stmtObj.close();
                } catch (SQLException e) {
                }
            }
        }
        if (hasNext) {
            SetErrorStatus("Username is already taken somebody");
            return false;
        }
        stmtObj = RequestDB("INSERT INTO " + _tableName + "(" + _nameFieldUserName + "," + _nameFieldPassword + ") VALUES('" + userName + "','" + password + "')", false);
        if (stmtObj != null) {
            try {
                stmtObj.close();
            } catch (SQLException e) {
            }
        }
        return GetQueryStatus();
    }

    public Boolean DeleteAccount(String userName, String password)
    {
        if (!CheckUsernamePassword(userName, password))
            return false;
        password = DigestUtils.md5Hex(password);
        Statement stmtObj = RequestDB("DELETE FROM " + _tableName + " WHERE " + _nameFieldUserName + "= '"+userName + "' AND "+_nameFieldPassword + "='" + password + "'", false);
        if (stmtObj != null) {
            try {
                stmtObj.close();
            } catch (SQLException e) {
            }
        }
        return GetQueryStatus();
    }

    public String LogIn(String userName, String password)
    {
        String token = "";
        if (!CheckUsernamePassword(userName, password))
            return token;
        password = DigestUtils.md5Hex(password);
        Statement stmtObj = RequestDB("SELECT * FROM " + _tableName + " WHERE " + _nameFieldUserName + "='" + userName + "' AND " + _nameFieldPassword + "='" + password + "'", true);
        if (!GetQueryStatus()) {
            return token;
        }
        Boolean hasNext = false;
        try {
            hasNext = resObj.next();
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
            return token;
        } finally {
            if (stmtObj != null) {
                try {
                    stmtObj.close();
                } catch (SQLException e) {
                }
            }
        }
        if (!hasNext) {
            SetErrorStatus("Invalid login or password");
            return token;
        }
        token = GenerateUniqueToken();
        stmtObj = RequestDB("UPDATE " + _tableName + " SET " + _nameFieldToken + "='" + token + "'," +_nameFieldTime+"="+String.valueOf(System.currentTimeMillis())
                + " WHERE " + _nameFieldUserName + "='" + userName + "' AND " + _nameFieldPassword + "='" + password + "'", false);
        if (stmtObj != null) {
            try {
                stmtObj.close();
            } catch (SQLException e) {
            }
        }
        return token;
    }

    /*public String LogIn(String userName, int APIKey, int publicKey, int sole)
    {
        if(!_dbStatus)
        {
            _errorMessage = "You are not connected for DB";
            _queryStatus = false;
            return "";
        }
        if(!CheckValid(userName))
        {
            _errorMessage = "Invalid Username";
            _queryStatus = false;
            return "";
        }
        String token;
        token = GenerateUniqueToken();
        if(!RequestDB("INSERT INTO " +_tableName + "(" + _nameFieldUserName + "," + _nameFieldToken + ") VALUES(" + userName + "," + token+")"))
            return "";
        return token;
    }*/

    public Boolean LogOut(String token)
    {
        if (!updateToken(token))
            return false;
        RequestDB("UPDATE " + _tableName + " SET " + _nameFieldToken + "= '' WHERE " + _nameFieldToken + "='" + token + "'", false);
        return GetQueryStatus();
    }

    public Boolean HasLogged(String token)
    {
        if (!updateToken(token))
            return false;
        Statement stmtObj = RequestDB("SELECT * FROM " + _tableName + " WHERE " + _nameFieldToken + "='" + token + "'" +" AND time+"+_timeLiveTokenms+">="+String.valueOf(System.currentTimeMillis()), true);
        if (!GetQueryStatus())
            return false;
        Boolean hasNext = false;
        try {
            hasNext = resObj.next();
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
            return false;
        } finally {
            if (stmtObj != null) {
                try {
                    stmtObj.close();
                } catch (SQLException e) {
                }
            }
        }
        return hasNext;
    }

    public String GetUserName(String token)
    {
        return GetValueFromDb(token, _nameFieldUserName);
    }

    public String GetRole(String token)
    {
        return GetValueFromDb(token,"Role");
    }

    public String GetPassword(String token)
    {
        return GetValueFromDb(token, _nameFieldPassword);
    }

    private Boolean CreateConnection()
    {
        try {
            _connection = DriverManager.getConnection(_stringConnection);
        } catch (SQLException e) {
            _errorMessage = e.getMessage();
            return false;
        }
        return true;
    }

    private Boolean CheckValid(String value)
    {
        if (value == null)
            return false;
        if (value.contains(" "))
            return false;
        if (value.isEmpty())
            return false;
        return true;
    }

    private String GetValueFromDb(String token, String nameField)
    {
        if (!updateToken(token))
            return "";
        Statement stmtObj = RequestDB("SELECT " + nameField + " FROM " + _tableName + " WHERE " + _nameFieldToken + "='" + token + "'", true);
        if (!GetQueryStatus())
            return "";
        String outputString = "";
        try {
            resObj.next();
            outputString = resObj.getString(_nameFieldUserName);
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
            return "";
        } finally {
            if (stmtObj != null) {
                try {
                    stmtObj.close();
                } catch (SQLException e) {
                }
            }
        }
        return outputString;
    }

    private Statement RequestDB(String sql, Boolean response)
    {
        _lastRequest = sql;
        _queryStatus = true;
        if (!_dbStatus) {
            SetErrorStatus("You are not connected for DB");
            return null;
        }
        Statement stmtObj = null;
        try {
            stmtObj = _connection.createStatement();
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
            return null;
        }
        try {
            if (response)
                resObj = stmtObj.executeQuery(sql);
            else
                stmtObj.execute(sql);
        } catch (SQLException e) {
            SetErrorStatus(e.getMessage());
        } finally {
            return stmtObj;
        }
    }

    public String GenerateUniqueToken()
    {
        String token = "";
        if (!_dbStatus) {
            SetErrorStatus("You are not connected for DB");
            return "";
        }
        Boolean condition = true;
        while (condition) {
            token = String.valueOf((new Random().nextInt(999999999)));
            Statement stmtObj = RequestDB("SELECT * FROM " + _tableName + " WHERE " + _nameFieldToken + "=" + token, true);
            if (!GetQueryStatus())
                return "";
            try {
                condition = resObj.next();
            } catch (SQLException e) {
                SetErrorStatus(e.getMessage());
                return "";
            } finally {
                if (stmtObj != null) {
                    try {
                        stmtObj.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        ;
        return token;
    }

    private Boolean updateToken(String token)
    {
        _queryStatus = true;
        if (!_dbStatus) {
            SetErrorStatus("You are not connected for DB");
            return false;
        }
        if (!CheckValid(token)) {
            SetErrorStatus("Invalid token");
            return false;
        }
        return true;
    }

    private Boolean CheckUsernamePassword(String username, String password)
    {
        _queryStatus = true;
        if (!_dbStatus) {
            SetErrorStatus("You are not connected for DB");
            return false;
        }
        if (!CheckValid(username) || !CheckValid(password)) {
            SetErrorStatus("Invalid Username or Password");
            return false;
        }
        return true;
    }

    private void SetErrorStatus(String message)
    {
        _errorMessage = message;
        _queryStatus = false;
    }
}