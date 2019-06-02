package com.rsoi2app.controllers;

import com.rsoi2app.config.Startup;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;

@Controller
public class WebRTC {
    @GetMapping("/Video/CallRequest")
    public String setCallRequest(@CookieValue(name="Token", defaultValue="") String token,
                                 @RequestParam(name="username", required=false, defaultValue= "") String username,
                                 @RequestParam(name="firstTime", required=false, defaultValue= "") String firstTime,
                                 @RequestParam(name="countfromicecandidates", required=false, defaultValue= "") String countfromicecandidates,
                                 HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            Utils.requestForService(Startup.GetWebrtcService() +
                            "/Video/CallRequest?username=" +
                            username + "&firstTime=" +
                            firstTime + "&countfromicecandidates=" +
                            countfromicecandidates + "&usernamefrom=" +
                            usernameFrom,
                    "none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @PostMapping("/Video/CallRequestFromIceCandidates")
    public String setCallRequestFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                                  @RequestBody String jsonInputString,
                                                  HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            Utils.requestPostForService(Startup.GetWebrtcService() +
                            "/Video/CallRequestFromIceCandidates?usernamefrom="+ usernameFrom,
                    jsonInputString, "none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @PostMapping("/Video/CallRequestToIceCandidates")
    public String setCallRequestToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                                @RequestParam(name="username", required=false, defaultValue= "") String username,
                                                @RequestBody String jsonInputString,
                                                HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            Utils.requestPostForService(Startup.GetWebrtcService() +
                            "/Video/CallRequestToIceCandidates?username=" + username+
                            "&usernamefrom="+usernameFrom,
                    jsonInputString, "none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @PostMapping("/Video/CallRequestFromDescription")
    public String setCallRequestFromDescription(@CookieValue(name="Token", defaultValue="") String token,
                                                @RequestBody String jsonInputString,
                                                HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            Utils.requestPostForService(Startup.GetWebrtcService() + "/Video/CallRequestFromDescription" +
                            "?usernamefrom=" + usernameFrom,
                    jsonInputString, "none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @PostMapping("/Video/CallRequestToDescription")
    public String setCallRequestToDescription(@CookieValue(name="Token", defaultValue="") String token,
                                              @RequestParam(name="username", required=false, defaultValue= "") String username,
                                              @RequestBody String jsonInputString,
                                              HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
        Utils.requestPostForService(Startup.GetWebrtcService()+"/Video/CallRequestToDescription?username="+username +
                        "&usernamefrom=" + usernameFrom,
                jsonInputString,"none",Utils.Services.WebRTC);
    }
        return "greeting";
    }

    @GetMapping("/Video/CallAnswer")
    public String setCallAnswer(@CookieValue(name="Token", defaultValue="") String token,
                                @RequestParam(name="username", required=false, defaultValue= "") String username,
                                @RequestParam(name="status", required=false, defaultValue= "") String status,
                                @RequestParam(name="counttoicecandidates", required=false, defaultValue= "0") String counttoicecandidates,
                                HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            Utils.requestForService(Startup.GetWebrtcService() + "/Video/CallAnswer?username=" + username +
                    "&status=" + status +
                    "&counttoicecandidates=" + counttoicecandidates +
                    "&usernamefrom=" + usernameFrom, "none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetCallAnswer", produces = "application/json")
    @ResponseBody
    public String getCallAnswer(@CookieValue(name="Token", defaultValue="") String token,
                                @RequestParam(name="username", required=false, defaultValue= "") String username,
                                HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
        return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetCallAnswer?username="+username +
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }


    @RequestMapping(method=RequestMethod.GET ,value="/Video/MyCallRequest", produces="application/json")
    @ResponseBody
    public String myCallRequest(@CookieValue(name="Token", defaultValue="") String token,
                                HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/MyCallRequest" +
                "?usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetFromIceCandidates", produces="application/json")
    @ResponseBody
    public String getFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                       @RequestParam(name="username", required=false, defaultValue= "") String username,
                                       HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetFromIceCandidates?username="+username+
                    "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetCountToIceCandidates", produces="application/json")
    @ResponseBody
    public String getCountToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                          @RequestParam(name="username", required=false, defaultValue= "") String username,
                                          HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetCountToIceCandidates?username="+username+
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetCountFromIceCandidates", produces="application/json")
    @ResponseBody
    public String getCountFromIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                            @RequestParam(name="username", required=false, defaultValue= "") String username,
                                            HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetCountFromIceCandidates?username="+username+
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }


    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetToIceCandidates", produces="application/json")
    @ResponseBody
    public String getToIceCandidates(@CookieValue(name="Token", defaultValue="") String token,
                                     @RequestParam(name="username", required=false, defaultValue= "") String username,
                                     HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetToIceCandidates?username="+username+
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetFromDescription", produces="application/json")
    @ResponseBody
    public String getFromDescription(@CookieValue(name="Token", defaultValue="") String token,
                                     @RequestParam(name="username", required=false, defaultValue= "") String username,
                                     HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetFromDescription?username="+username+
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }

    @RequestMapping(method=RequestMethod.GET ,value="/Video/GetToDescription", produces="application/json")
    @ResponseBody
    public String getToDescription(@CookieValue(name="Token", defaultValue="") String token,
                                   @RequestParam(name="username", required=false, defaultValue= "") String username,
                                   HttpServletResponse response) throws IOException, ParseException {
        Utils.UserInfo info = Utils.GetUserInfo(token);
        if(info.getStatus() && info.getLogged()) {
            String usernameFrom = info.getUsername();
            return Utils.requestForService(Startup.GetWebrtcService()+"/Video/GetToDescription?username="+username+
                "&usernamefrom=" + usernameFrom,"none",Utils.Services.WebRTC);
        }
        return "greeting";
    }
}
