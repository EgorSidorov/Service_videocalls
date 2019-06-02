package com.rsoi2app.controllers;

import com.rsoi2app.config.Startup;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class Logs {
    @GetMapping("/Account_logs")
    public String accountLogs(@CookieValue(name="Token", defaultValue= "") String token,
                              @RequestParam(name="page", required=false, defaultValue= "") String page,
                              Model model,
                              HttpServletResponse response) throws IOException, ParseException {
        if(!token.isEmpty() && !token.contains(" ") && !page.contains(" "))
        {
            boolean status1 = false;
            JSONObject userJson = new JSONObject();
            String logs = "";
            Utils.UserInfo info = Utils.GetUserInfo(token);
            if(info.getStatus() && info.getLogged()) {
                String usernameFrom = info.getUsername();
                model.addAttribute("username",usernameFrom);
            }
            else {
                response.setStatus(500);
                return "redirect:https://speakstars.ru/login";
            }
            String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/Logs?page="+page,token,Utils.Services.Account);
            if (!responseStr1.contains("Error")) {
                JSONParser parser = new JSONParser();
                userJson = (JSONObject) parser.parse(responseStr1);
                if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
                    logs = userJson.get("logs").toString();
                    status1 = true;
                }
            }
            if(status1)
                model.addAttribute("name",logs);
            else
                model.addAttribute("name","Необходима роль мастера.");
        }
        else {
            model.addAttribute("name","Error parameters");
            response.setStatus(400);
        }
        return "statistics";
    }

    @GetMapping("/Calls_logs")
    public String callsLogs(
            @CookieValue(name="Token", defaultValue= "") String token,
            @RequestParam(name="page", required=false, defaultValue= "") String page,
            Model model,
            HttpServletResponse response) throws IOException, ParseException {
        if(!page.contains(" "))
        {
            boolean status1 = false;
            JSONObject userJson = new JSONObject();
            String logs = "";
            Utils.UserInfo info = Utils.GetUserInfo(token);
            if(info.getStatus() && info.getLogged()) {
                String usernameFrom = info.getUsername();
                model.addAttribute("username",usernameFrom);
            }
            else {
                response.setStatus(500);
                return "redirect:https://speakstars.ru/login";
            }
            String responseStr1 = Utils.requestForService(Startup.GetGatewayHostPort()+"/service/calls/Logs?page="+page,"none",Utils.Services.Calls);
            if (!responseStr1.contains("Error")) {
                JSONParser parser = new JSONParser();
                userJson = (JSONObject) parser.parse(responseStr1);
                if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
                    logs = userJson.get("logs").toString();
                    status1 = true;
                }
            }
            if(status1)
                model.addAttribute("name",logs);
            else
                model.addAttribute("name","Необходима роль мастера.");
        }
        else {
            model.addAttribute("name","Error parameters");
            response.setStatus(400);
        }
        return "statistics";
    }

    @GetMapping("/Payment_logs")
    public String paymentLogs(
            @CookieValue(name="Token", defaultValue= "") String token,
            @RequestParam(name="page", required=false, defaultValue= "") String page,
            Model model,
            HttpServletResponse response) throws IOException, ParseException {
        if(!page.contains(" "))
        {
            boolean status1 = false;
            JSONObject userJson = new JSONObject();
            String logs = "";
            Utils.UserInfo info = Utils.GetUserInfo(token);
            if(info.getStatus() && info.getLogged()) {
                String usernameFrom = info.getUsername();
                model.addAttribute("username",usernameFrom);
            }
            else {
                response.setStatus(500);
                return "redirect:https://speakstars.ru/login";
            }
            String responseStr1 = Utils.requestForService(Startup.GetGatewayHostPort()+"/service/payment/Logs?page="+page,"none",Utils.Services.Payment);
            if (!responseStr1.contains("Error")) {
                JSONParser parser = new JSONParser();
                userJson = (JSONObject) parser.parse(responseStr1);
                if(userJson.get("Status").equals("Success") && userJson.containsKey("logs")) {
                    logs = userJson.get("logs").toString();
                    status1 = true;
                }
            }
            if(status1)
                model.addAttribute("name",logs);
            else
                model.addAttribute("name","Необходима роль мастера.");
        }
        else {
            model.addAttribute("name","Error parameters");
            response.setStatus(400);
        }
        return "statistics";
    }
}
