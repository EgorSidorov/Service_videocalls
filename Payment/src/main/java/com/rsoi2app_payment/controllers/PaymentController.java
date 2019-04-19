package com.rsoi2app_payment.controllers;

import com.rsoi2app_payment.config.Startup;
import com.rsoi2app_payment.models.PaymentModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Controller
public class PaymentController {

	@GetMapping("/New_purse")
	@ResponseBody
	public HashMap<String, Object> NewPurse(@RequestParam(name="username", required=false, defaultValue= "") String username,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										  HttpServletResponse response) {
		PaymentModel model = new PaymentModel();
		model.SetLogs("/New_purse/?username="+username);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!username.isEmpty())
		{
			if(model.CreatePursy(username)) {
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



	@GetMapping("/Add_cash")
	@ResponseBody
	public HashMap<String, Object> AddCash(@RequestParam(name="username", required=false, defaultValue= "") String username,
										   @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										   @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										   @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											@RequestParam(name="cash", required=false, defaultValue= "") String cash,
										 	HttpServletResponse response) {
		PaymentModel model = new PaymentModel();
		model.SetLogs("/Add_cash/?username="+username+"&cash="+cash);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!username.isEmpty() && !cash.isEmpty())
		{
			if(model.AddCash(cash,username)) {
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

	@GetMapping("/Withdraw_cash")
	@ResponseBody
	public HashMap<String, Object> WithdrawCash(@RequestParam(name="username", required=false, defaultValue= "") String username,
												@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												@RequestParam(name="cash", required=false, defaultValue= "") String cash,
												HttpServletResponse response) {
		PaymentModel model = new PaymentModel();
		model.SetLogs("/Withdraw_cash/?username=" + username + "&cash=" + cash);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if (!username.isEmpty() && !cash.isEmpty()) {
			if (model.WithdrawCash(cash,username)) {
				setStatus(200, response, jsonAnswer);
			} else {
				if (!model.GetDbStatus()) {
					setStatus(500, response, jsonAnswer);
				} else {
					setStatus(404, response, jsonAnswer);
				}
			}
		} else {
			setStatus(401, response, jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Show_cash")
	@ResponseBody
	public HashMap<String, Object> ShowCash(@RequestParam(name="username", required=false, defaultValue= "") String username,
											@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											HttpServletResponse response) {
		PaymentModel model = new PaymentModel();
		model.SetLogs("/Show_cash/?username="+username);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!username.isEmpty())
		{
			String cash = model.ShowCash(username);
			if(model.GetQueryStatus()) {
				setStatus(200,response,jsonAnswer);
				jsonAnswer.put("cash",cash);
			}
			else {
				setStatus(500,response,jsonAnswer);
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
		PaymentModel model = new PaymentModel();
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

	@PostMapping("/GetServiceToken")
	@ResponseBody
	public HashMap<String, String> getServiceToken(@RequestBody String password) {
		PaymentModel model = new PaymentModel();
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
		String salt = generateToken();
		String token = org.apache.commons.codec.digest.DigestUtils.md5Hex(Startup.serviceLogin+Startup.servicePassword+salt+time);
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
	@ResponseBody
	public HashMap<String, Object> CheckServiceToken(@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													 HttpServletResponse response)
	{
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		PaymentModel model = new PaymentModel();
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