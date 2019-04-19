package com.rsoi2app_webrtc;

import com.rsoi2app_webrtc.config.Startup;
import com.rsoi2app_webrtc.external.EasyAuth;
import com.rsoi2app_webrtc.models.AccountModel;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AccountModelTest {

    @Test
    public void createConnection() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            Mockito.when(model.CreateConnection()).thenReturn(true);
        Assert.assertEquals(true,model.CreateConnection());
    }


    @Test
    public void getAllRoles() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            model.resObj = Mockito.mock(ResultSet.class);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("Name_Role")).thenReturn("master").thenReturn("medium").thenReturn("beginner");
            Mockito.when(model.RequestDB("SELECT Name_Role FROM Account.Roles",true)).thenReturn(null);

        List<String> result = model.GetAllRoles();
        Assert.assertEquals(true,model.GetQueryStatus());
        Assert.assertEquals(3,result.size());
        Assert.assertEquals("medium",result.get(1));
    }



    @Test
    public void CreateUser() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.auth = Mockito.mock(EasyAuth.class);
            Mockito.when(model.auth.CreateAccount("Egor","qwerty")).thenReturn(false);

        Assert.assertEquals(false,model.CreateUser("Egor","qwerty","1"));
    }

    @Test
    public void Login() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.auth = Mockito.mock(EasyAuth.class);
            Mockito.when(model.auth.LogIn("Egor2","qwerty")).thenReturn("some_token");
            Mockito.when(model.auth.GetQueryStatus()).thenReturn(true);
        String token = model.Login("Egor2","qwerty");
        Assert.assertEquals(true,model.GetQueryStatus());
        Assert.assertEquals(false,token.isEmpty());
    }


    @Test
    public void getUserNames() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.auth = Mockito.mock(EasyAuth.class);
            model.connection = Mockito.mock(Connection.class);
            model.resObj = Mockito.mock(ResultSet.class);
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            Mockito.when(model.IsLogged("test_token")).thenReturn(true);
            Mockito.when(model.auth.LogIn("Egor3","qwerty")).thenReturn("test_token");
            Mockito.when(model.auth.GetQueryStatus()).thenReturn(true);
            Mockito.when(model.RequestDB("SELECT Username,  Name_Role FROM Account.Info us JOIN Account.Roles rl ON(us.Role=rl.ID_Role) LIMIT 2 OFFSET 0",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("Username")).thenReturn("Egor");
            Mockito.when(model.resObj.getString("Name_Role")).thenReturn("master");

        String token = model.Login("Egor3","qwerty");
        List<String> listString = model.GetUserNames(0);
        Assert.assertEquals(true,model.GetQueryStatus());
        //Assert.assertEquals("Egor:master",listString.get(0));
    }


    @Test
    public void GetUsername() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.auth = Mockito.mock(EasyAuth.class);
            Mockito.when(model.auth.GetQueryStatus()).thenReturn(true);
            Mockito.when(model.auth.GetUserName("test_token")).thenReturn("Egor5");
            Mockito.when(model.auth.LogIn("Egor5","qwerty")).thenReturn("test_token");
        String token = model.Login("Egor5","qwerty");
        Assert.assertEquals("Egor5",model.GetUsername(token));
    }


    @Test
    public void GetRole() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.resObj = Mockito.mock(ResultSet.class);
            Mockito.when(model.RequestDB("SELECT Name_Role FROM Account.Roles rl JOIN Account.Info inf ON(inf.Role = rl.ID_Role) WHERE inf.Cookie=test_token",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("Name_Role")).thenReturn("master");
        String token = model.Login("Egor6","qwerty");
        //Assert.assertEquals("master",model.GetRole(token));
    }

    @Test
    public void GetLogs() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            model.resObj = Mockito.mock(ResultSet.class);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("SELECT str FROM Account.Logger LIMIT 2 OFFSET 0",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("str")).thenReturn("log string");
        List<String>logs = model.GetLogs(0);
        Assert.assertEquals("log string",logs.get(0));
    }

    @Test
    public void SetLogs() throws Exception {
        AccountModel model;
            model = Mockito.spy(new AccountModel());
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("INSERT INTO Account.Logger (str) VALUES('log string')",false)).thenReturn(null);
        Assert.assertEquals(true,model.SetLogs("log string"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("INSERT INTO Account.Logger (str) VALUES('log string')",false);
    }

}
