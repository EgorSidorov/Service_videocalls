package com.rsoi2app_calls;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import  com.rsoi2app_calls.models.CallsModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CallsModelTest {



    @Test
    public void AddCall() throws Exception {
        CallsModel model;
        {
            model = Mockito.spy(new CallsModel());
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            Mockito.when(model.RequestDB("INSERT INTO Calls.History (Duration, Username) VALUES(20,'Egor30')",false)).thenReturn(null);
        }
        //Assert.assertEquals(true,model.AddCall("20","Egor30"));
    }

    @Test
    public void ShowAllHistory() throws Exception {
        CallsModel model;
        {
            model = Mockito.spy(new CallsModel());
            model.connection = Mockito.mock(Connection.class);
            model.resObj = Mockito.mock(ResultSet.class);
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            Mockito.when(model.RequestDB("SELECT Duration, Username FROM Calls.History hs WHERE hs.Username ='Egor31' LIMIT 2 OFFSET 0",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("Duration")).thenReturn("20");
            Mockito.when(model.resObj.getString("Username")).thenReturn("Egor31");
        }
        //Assert.assertEquals("20 Egor31",model.ShowCallHistory("Egor31",0).get(0));
    }

    @Test
    public void GetLogs() throws Exception {
        CallsModel model;
        {
            model = Mockito.spy(new CallsModel());
            model.resObj = Mockito.mock(ResultSet.class);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("SELECT str FROM Calls.Logger LIMIT 2 OFFSET 0",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("str")).thenReturn("log string");
        }
        List<String> logs = model.GetLogs(0);
        Assert.assertEquals("log string",logs.get(0));
    }

    @Test
    public void SetLogs() throws Exception {
        CallsModel model;
        {
            model = Mockito.spy(new CallsModel());
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("INSERT INTO Calls.Logger (str) VALUES('log string')",false)).thenReturn(null);
        }
        Assert.assertEquals(true,model.SetLogs("log string"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("INSERT INTO Calls.Logger (str) VALUES('log string')",false);
    }

}