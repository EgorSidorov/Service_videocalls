package com.rsoi2app_webrtc.controllers;

import com.rsoi2app_webrtc.models.AccountModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class OAuth {

    @GetMapping("/grantRules")
    @ResponseBody
    public HashMap<String, Object> grantRules(@RequestParam(name="appID", defaultValue= "") String appID,
                                            @CookieValue(name="Token", defaultValue="") String token,
                                              @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
                                              @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
                                              @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
                                            HttpServletResponse response){
        HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
        AccountModel model = new AccountModel();
        if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
        {
            AccountController.setStatus(405,response,jsonAnswer);
            try {
                model.finalize();
            } catch (SQLException exc) {}
            return jsonAnswer;
        }
        if(appID.isEmpty())
        {
            response.setStatus(400);
            jsonAnswer.put("Status","Error");
            jsonAnswer.put("Status message","Bad Request");
        } else {
            if (model.IsLogged(token)) {
                jsonAnswer.put("Status", "Success");
                String authToken = generateToken();
                jsonAnswer.put("Auth token", authToken);
                jsonAnswer.put("redirectURI",model.getRedirectURI(appID));
                jsonAnswer.put("scope",model.getScope(appID));
                jsonAnswer.put("Username",model.GetUsername(token));
                model.SetAuthToken(model.GetUsername(token),appID,authToken);
            } else {
                response.setStatus(401);
                jsonAnswer.put("Status", "Error");
                jsonAnswer.put("Status message", "Unauthorized");
            }
        }
        try {
            model.finalize();
        } catch (SQLException exc) {}
        return jsonAnswer;
    }

    @PostMapping("/accessToken")
    @ResponseBody
    public HashMap<String, Object> accessToken(@RequestParam(name="appID", defaultValue= "-1") String appID,
                                              @RequestParam(name="username", defaultValue= "-1") String username,
                                              @RequestParam(name="authToken", defaultValue= "-1") String authToken,
                                               @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
                                               @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
                                               @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
                                              @RequestBody String secret,
                                              HttpServletResponse response){
        AccountModel model = new AccountModel();
        HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
        model.SetLogs("/accessToken secret="+secret);
        if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
        {
            AccountController.setStatus(405,response,jsonAnswer);
            try {
                model.finalize();
            } catch (SQLException exc) {}
            return jsonAnswer;
        }
        if(appID.isEmpty())
        {
            response.setStatus(400);
            jsonAnswer.put("Status","Error");
            jsonAnswer.put("Status message","Bad Request");
        } else {
            if (model.checkAuthToken(username,appID,authToken,secret)) {
                jsonAnswer.put("Status", "Success");
                String accessToken = generateToken();
                jsonAnswer.put("Access token", accessToken);
                String refreshToken = generateToken();
                jsonAnswer.put("Refresh token", refreshToken);
                model.SetAccessToken(username,appID,accessToken,refreshToken);
            } else {
                response.setStatus(406);
                jsonAnswer.put("Status", "Error");
                jsonAnswer.put("Status message", "Not Acceptable");
            }
        }
        try {
            model.finalize();
        } catch (SQLException exc) {}
        return jsonAnswer;
    }

    @PostMapping("/refreshToken")
    @ResponseBody
    public HashMap<String, Object> refreshToken(@RequestParam(name="appID", defaultValue= "-1") String appID,
                                               @RequestParam(name="username", defaultValue= "-1") String username,
                                               @RequestParam(name="refreshToken", defaultValue= "-1") String oldrefreshToken,
                                                @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
                                                @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
                                                @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
                                                @RequestBody String secret,
                                               HttpServletResponse response){
        HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
        AccountModel model = new AccountModel();
        if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
        {
            AccountController.setStatus(405,response,jsonAnswer);
            try {
                model.finalize();
            } catch (SQLException exc) {}
            return jsonAnswer;
        }
        if(appID.isEmpty())
        {
            response.setStatus(400);
            jsonAnswer.put("Status","Error");
            jsonAnswer.put("Status message","Bad Request");
        } else {
            if (model.checkRefreshToken(username,appID,oldrefreshToken,secret)) {
                jsonAnswer.put("Status", "Success");
                String accessToken = generateToken();
                jsonAnswer.put("Access token", accessToken);
                String refreshToken = generateToken();
                jsonAnswer.put("Refresh token", refreshToken);
                model.SetAccessToken(username,appID,accessToken,refreshToken);
            } else {
                response.setStatus(406);
                jsonAnswer.put("Status", "Error");
                jsonAnswer.put("Status message", "Not Acceptable");
            }
        }
        try {
            model.finalize();
        } catch (SQLException exc) {}
        return jsonAnswer;
    }

    public static String generateToken(){
        int token = new Random().nextInt(999999);
        return String.valueOf(token);
    }
}
