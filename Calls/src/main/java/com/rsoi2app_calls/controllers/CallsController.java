package com.rsoi2app_calls.controllers;

import com.rsoi2app_calls.config.Startup;
import com.rsoi2app_calls.models.CallsModel;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Controller
public class CallsController {

	@GetMapping("/New")
	@ResponseBody
	public HashMap<String, Object> Login(@RequestParam(name="username", required=false, defaultValue= "") String username,
										  @RequestParam(name="duration", required=false, defaultValue= "") String duration,
										 @RequestParam(name="usernameto", required=false, defaultValue= "") String usernameto,
										 @RequestParam(name="updatelast", required=false, defaultValue= "false")String updatelast,
										 @RequestParam(name="type", required=false, defaultValue= "false")String type,
										 @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										  HttpServletResponse response) {
		java.util.Date dt = new java.util.Date();
		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(dt);
		CallsModel model = new CallsModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		model.SetLogs("/New/?username="+username+"&duration="+duration);
		if(!username.isEmpty() && !duration.isEmpty())
		{
			if(model.AddCall(duration,username,usernameto,currentTime,Boolean.getBoolean(updatelast),type)) {
				setStatus(201,response,jsonAnswer);
			}
			else {
				setStatus(500,response,jsonAnswer);
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


	@GetMapping("/Show")
	@ResponseBody
	public HashMap<String, Object> UserList(@RequestParam(name="username", required=false, defaultValue= "") String username,
											@RequestParam(name="page", required=false, defaultValue= "0") String page,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										 	HttpServletResponse response) {
		CallsModel model = new CallsModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		model.SetLogs("/Show/?username="+username+"&page="+page);
		if(!username.isEmpty())
		{
			List<String> callsHistory = model.ShowCallHistory(username,Integer.parseInt(page));
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("History",callsHistory);
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

	@GetMapping("/Logs")
	@ResponseBody
	public HashMap<String, Object> Logs(@RequestParam(name="page", required=false, defaultValue= "0") String page,
										@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										HttpServletResponse response) {
		CallsModel model = new CallsModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
				List<String> logs = model.GetLogs(Integer.parseInt(page));
				if (model.GetQueryStatus()){
					setStatus(200, response, jsonAnswer);
					jsonAnswer.put("logs", logs);
				}
				else {
					setStatus(500,response,jsonAnswer);
				}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/GetServiceToken")
	@ResponseBody
	public HashMap<String, String> GetServiceToken() {
		HashMap<String, String> jsonAnswer = new LinkedHashMap<String, String>();
		String time = String.valueOf(System.currentTimeMillis() + Startup.timelivems);
		String salt = generateToken();
		String token = org.apache.commons.codec.digest.DigestUtils.md5Hex(Startup.serviceLogin+Startup.servicePassword+salt+time);
		jsonAnswer.put("token",token);
		jsonAnswer.put("salt",salt);
		jsonAnswer.put("time",time);
		CallsModel model = new CallsModel();
		model.SetLogs("/GetServiceToken?serviceToken="+token+
				"&serviceSalt="+salt+"&serviceTime="+time);
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/CheckServiceToken")
	@ResponseBody
	public HashMap<String, Object> CheckServiceToken(@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													 HttpServletResponse response)
	{
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		CallsModel model = new CallsModel();
		model.SetLogs("/CheckServiceToken?serviceToken="+serviceToken+
				"&serviceSalt="+serviceSalt+"&serviceTime="+serviceTime);
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
			setStatus(405,response,jsonAnswer);
		else
			setStatus(200,response,jsonAnswer);
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	public static String generateToken(){
		int token = new Random().nextInt(999999);
		return String.valueOf(token);
	}


	void setStatus(int status, HttpServletResponse response, HashMap<String, Object> jsonAnswer)
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