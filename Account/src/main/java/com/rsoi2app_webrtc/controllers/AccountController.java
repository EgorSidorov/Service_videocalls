package com.rsoi2app_webrtc.controllers;

import com.rsoi2app_webrtc.config.Startup;
import com.rsoi2app_webrtc.models.AccountModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class AccountController {

	@PostMapping("/Login")
	@ResponseBody
	public HashMap<String, Object> login(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestBody String password,
										 @RequestParam(name="serviceToken",defaultValue = "-1") String serviceToken,
										 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										 @RequestParam(name="serviceTime",defaultValue = "-1") String serviceTime,
										  HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		model.SetLogs("/Login?username="+username+"&password="+password+"&serviceToken="+serviceToken+
				"&serviceSalt="+serviceSalt+"&serviceTime="+serviceTime);
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!username.isEmpty() && !password.isEmpty())
		{
			String authCookie = model.Login(username,password);
			if(model.GetQueryStatus()) {
				response.addCookie(new Cookie("Token", authCookie));
				jsonAnswer.put("Cookie",authCookie);
				setStatus(200,response,jsonAnswer);
			}
			else {
					if(!model.GetDbStatus()) {
						setStatus(500,response,jsonAnswer);
					} else {
						setStatus(404,response,jsonAnswer);
					}
			}
		}
		else {
			setStatus(400,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@RequestMapping(value = "/UsersList",  method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public HashMap<String, Object> userList(@RequestParam(name="page", required=false, defaultValue= "0") String page,
											@RequestBody(required = false) String accessToken,
											@RequestParam(name = "username", required = false, defaultValue = "-1") String username,
											@RequestParam(name = "appId", required = false, defaultValue = "-1") String appId,
											@CookieValue(name="Token", defaultValue="") String token,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										 	HttpServletResponse response) {
		if(appId.equals("-1"))
			accessToken = "";
		AccountModel model = new AccountModel();
		model.SetLogs("/UsersList?Token="+token+"&page="+page);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!accessToken.isEmpty() && model.checkAccessToken(username,appId,accessToken))
		{
			List<String> userList = model.GetUserNames(Integer.parseInt(page));
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserList",userList);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(404,response,jsonAnswer);
				}
			}
		}
		else if(!token.isEmpty())
		{
			List<String> userList = model.GetUserNames(Integer.parseInt(page));
			if(model.GetQueryStatus() && model.IsLogged(token)) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserList",userList);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(404,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Logout")
	@ResponseBody
	public HashMap<String, Object> userList(@CookieValue(name="Token", defaultValue="") String token,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!token.isEmpty())
		{
			if(model.Logout(token)) {
				setStatus(200,response,jsonAnswer);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(404,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/RoleList")
	@ResponseBody
	public HashMap<String, Object> user(@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/RoleList");
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
			List<String> userRoles = model.GetAllRoles();
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("UserRoles",userRoles);
			}
			else {
					setStatus(500,response,jsonAnswer);
			}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@GetMapping("/UserInfo")
	@ResponseBody
	public HashMap<String, Object> userInfo(@CookieValue(name="Token", defaultValue="") String token,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		model.SetLogs("/UserInfo?Token="+token);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!token.isEmpty())
		{
			boolean IsLogged = model.IsLogged(token);
			boolean queryStatus1 = IsLogged;
			String role = model.GetRole(token);
			boolean queryStatus2 = model.GetQueryStatus();
			String username = model.GetUsername(token);
			boolean queryStatus3 = model.GetQueryStatus();
			if(queryStatus1 & queryStatus2 & queryStatus1) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("IsLogged",IsLogged);
				jsonAnswer.put("username",username);
				jsonAnswer.put("role",role);
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}


	@PostMapping("/Create")
	@ResponseBody
	public HashMap<String, Object> create(@RequestParam(name="username", required=false, defaultValue= "") String username,
										@RequestBody String password,
										 @RequestParam(name="role", required=false, defaultValue= "2") String role,
										@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		AccountModel model = new AccountModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		model.SetLogs("/Create?username="+username+"&password="+password+"&role="+role);
		if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
			if (model.CreateUser(username, password, role)) {
				setStatus(200, response, jsonAnswer);
			} else {
				if (!model.GetDbStatus()) {
					setStatus(500, response, jsonAnswer);
				} else {
					setStatus(406, response, jsonAnswer);
				}
			}
		} else {
			setStatus(400, response, jsonAnswer);
	}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@PostMapping("/Delete")
	@ResponseBody
	public HashMap<String, Object> delete(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestBody String password,
										  @RequestParam(name="role", required=false, defaultValue= "2") String role,
										  @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										  HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		AccountModel model = new AccountModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		model.SetLogs("/Create?username="+username+"&password="+password+"&role="+role);
		if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
			if (model.DeleteUser(username, password, role)) {
				setStatus(200, response, jsonAnswer);
			} else {
				if (!model.GetDbStatus()) {
					setStatus(500, response, jsonAnswer);
				} else {
					setStatus(406, response, jsonAnswer);
				}
			}
		} else {
			setStatus(400, response, jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@PostMapping("/GetServiceToken")
	@ResponseBody
	public HashMap<String, String> getServiceToken(@RequestBody String password) {
		AccountModel model = new AccountModel();
		model.SetLogs("/GetServiceToken");
		HashMap<String, String> jsonAnswer = new LinkedHashMap<String, String>();
		if(!password.equals(Startup.servicePassword)){
			try {
				model.finalize();
			} catch (SQLException exc) {}
			jsonAnswer.put("token","");
			jsonAnswer.put("salt","");
			jsonAnswer.put("time","");
			return jsonAnswer;
		}
		String time = String.valueOf(System.currentTimeMillis() + Startup.timelivems);
		String salt = OAuth.generateToken();
		String token = DigestUtils.md5Hex(Startup.serviceLogin+Startup.servicePassword+salt+time);
		jsonAnswer.put("token",token);
		jsonAnswer.put("salt",salt);
		jsonAnswer.put("time",time);
		model.SetLogs("/GetServiceToken?serviceToken="+token+
				"&serviceSalt="+salt+"&serviceTime="+time);
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/CheckServiceToken")
	public HashMap<String, Object> checkServiceToken(@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													 HttpServletResponse response)
	{
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		AccountModel model = new AccountModel();
		model.SetLogs("/CheckServiceToken?serviceToken="+serviceToken+
				"&serviceSalt="+serviceSalt+"&serviceTime="+serviceTime);
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
			setStatus(401,response,jsonAnswer);
		else
			setStatus(200,response,jsonAnswer);
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Logs")
	@ResponseBody
	public HashMap<String, Object> logs(@CookieValue(name="Token", defaultValue="") String token,
										@RequestParam(name="page", required=false, defaultValue= "0") String page,
										@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											HttpServletResponse response) {
		AccountModel model = new AccountModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!token.isEmpty())
		{
			if(model.IsLogged(token)) {
				List<String> logs = model.GetLogs(Integer.parseInt(page));
				if (model.GetQueryStatus()){
					setStatus(200, response, jsonAnswer);
					jsonAnswer.put("logs", logs);
				}
				else {
					setStatus(500,response,jsonAnswer);
				}
			}
			else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	static void setStatus(int status, HttpServletResponse response, HashMap<String, Object> jsonAnswer)
	{
		response.setStatus(status);
		if(status == 200)//Success
		{
			jsonAnswer.put("Status","Success");
		}
		if(status == 201)//Created
		{
			jsonAnswer.put("Status","Success created");
		}
		if(status == 500)//Internal Server Error
		{
			jsonAnswer.put("Status", "Error");
			jsonAnswer.put("Status message", "Database unavailable");
		}
		if(status == 404)//Not Found
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Not Found");
		}
		if(status == 401)//Unauthorized
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Unauthorized");
		}
		if(status == 400)//Bad Request
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Missing parametres");
		}
		if(status == 406)//Not Acceptable
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Username is used");
		}
		if(status == 405)//Not Acceptable
		{
			jsonAnswer.put("Status","Error");
			jsonAnswer.put("Status message","Service Token is invalid");
		}
	}

}