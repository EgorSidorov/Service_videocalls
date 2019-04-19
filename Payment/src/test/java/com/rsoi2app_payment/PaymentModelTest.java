package com.rsoi2app_payment;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import com.rsoi2app_payment.models.PaymentModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PaymentModelTest {



    @Test
    public void WithdrawCash() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            model.connection = Mockito.mock(Connection.class);
        }
        Assert.assertEquals(true,model.WithdrawCash("20","Egor30"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("UPDATE Payment.Pursy SET Cash = Cash - 20 WHERE Username='Egor30'",false);
    }

    @Test
    public void NewPurse() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.GetDbStatus()).thenReturn(true);
        }
        Assert.assertEquals(true,model.CreatePursy("Egor31"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("INSERT INTO Payment.Pursy (Cash,Username) VALUES(0,'Egor31')",false);
    }

    @Test
    public void AddCash() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            model.connection = Mockito.mock(Connection.class);
        }
        Assert.assertEquals(true,model.AddCash("20","Egor30"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("UPDATE Payment.Pursy SET Cash = Cash + 20 WHERE Username='Egor30'",false);
    }

    @Test
    public void ShowCash() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            model.connection = Mockito.mock(Connection.class);
            model.resObj = Mockito.mock(ResultSet.class);
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            Mockito.when(model.RequestDB("SELECT Cash FROM Payment.Pursy WHERE Username='Egor31'",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("Cash")).thenReturn("20");
        }
        Assert.assertEquals("20",model.ShowCash("Egor31"));
    }

    @Test
    public void GetLogs() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            model.resObj = Mockito.mock(ResultSet.class);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("SELECT str FROM Payment.Logger LIMIT 2 OFFSET 0",true)).thenReturn(null);
            Mockito.when(model.resObj.next()).thenReturn(true).thenReturn(false);
            Mockito.when(model.resObj.getString("str")).thenReturn("log string");
        }
        List<String> logs = model.GetLogs(0);
        Assert.assertEquals("log string",logs.get(0));
    }

    @Test
    public void SetLogs() throws Exception {
        PaymentModel model;
        {
            model = Mockito.spy(new PaymentModel());
            Mockito.when(model.GetDbStatus()).thenReturn(true);
            model.connection = Mockito.mock(Connection.class);
            Mockito.when(model.RequestDB("INSERT INTO Payment.Logger (str) VALUES('log string')",false)).thenReturn(null);
        }
        Assert.assertEquals(true,model.SetLogs("log string"));
        Mockito.verify(model, Mockito.atLeastOnce()).RequestDB("INSERT INTO Payment.Logger (str) VALUES('log string')",false);
    }

}