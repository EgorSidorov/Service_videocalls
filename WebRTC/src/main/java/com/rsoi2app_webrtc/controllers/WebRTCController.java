package com.rsoi2app_webrtc.controllers;

import com.rsoi2app_webrtc.config.Startup;
import com.rsoi2app_webrtc.models.WebRTCModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Controller
public class WebRTCController {

	@GetMapping("/PhonebookList")
	@ResponseBody
	public HashMap<String, Object> PhonebookList(
			@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
			@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
			@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
			@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
			HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		//model.SetLogs("/PhonebookList?Token="+token);
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if(!usernamefrom.isEmpty())
		{
			List<String> userList = model.GetPhonebookNames(usernamefrom);
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
		else {
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Add_phonebook")
	@ResponseBody
	public HashMap<String, Object> AddPhoneBook(@RequestParam(name="username", defaultValue= "") String usernameto,
												@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
												@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
										  HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		if (!usernameto.isEmpty() && !usernameto.contains(" ")) {
			if (model.AddPhoneBook(usernamefrom, usernameto)) {
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

	@GetMapping("/Logs")
	@ResponseBody
	public HashMap<String, Object> Logs(@CookieValue(name="Token", defaultValue="") String token,
										@RequestParam(name="page", required=false, defaultValue= "0") String page,
										@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
										@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
										@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
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
			setStatus(401,response,jsonAnswer);
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/CallRequest")
	public String SetCallRequest(@RequestParam(name="usernamefrom", required=false, defaultValue="") String usernamefrom,
								 @RequestParam(name="username", required=false, defaultValue= "") String username,
								 @RequestParam(name="firstTime", required=false, defaultValue= "") String firstTime,
								 @RequestParam(name="countfromicecandidates", required=false, defaultValue= "") String countfromicecandidates,
								 @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
								 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
								 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
								 Model uimodel,
								 HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/Callrequest?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token)+"&firstTime="+firstTime);
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" ") && !firstTime.isEmpty() && !firstTime.contains(" "))
		{
			model.SetCallRequest( usernamefrom,username,Boolean.valueOf(firstTime),countfromicecandidates);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromIceCandidates")
	public String SetCallRequestFromIceCandidates(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
												  @RequestBody String jsonInputString,
												  @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												  Model uimodel,
												  HttpServletResponse response) throws ParseException {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/CallrequestFromIceCandidates?Token="+token);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestFromIceCandidates( usernamefrom,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToIceCandidates")
	public String SetCallRequestToIceCandidates(@RequestParam(name="username", required=false, defaultValue= "") String username,
												@RequestBody String jsonInputString,
												@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												Model uimodel,
												HttpServletResponse response) throws ParseException {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/CallrequestTomIceCandidates?Token="+token+"&username="+username);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestToIceCandidates( username,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestFromDescription")
	public String SetCallRequestFromDescription(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
												@RequestBody String jsonInputString,
												@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												Model uimodel,
												HttpServletResponse response) throws ParseException {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/CallrequestFromDescription?Token="+token);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestFromDescription( usernamefrom,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@PostMapping("/Video/CallRequestToDescription")
	public String SetCallRequestToDescription(@RequestParam(name="username", required=false, defaultValue= "") String username,
											  @RequestBody String jsonInputString,
											  @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
											  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
											  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
											  Model uimodel,
											  HttpServletResponse response) throws ParseException {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/CallrequestToDescription?Token="+token+"&username="+username+"&jsoninputstring="+jsonInputString);
		if(!jsonInputString.isEmpty())
		{
			model.SetCallRequestToDescription(username,jsonInputString);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@GetMapping("/Video/CallAnswer")
	public String SetCallAnswer(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
								@RequestParam(name="username", required=false, defaultValue= "") String username,
								@RequestParam(name="status", required=false, defaultValue= "") String status,
								@RequestParam(name="counttoicecandidates", required=false, defaultValue= "0") String counttoicecandidates,
								@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
								@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
								@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
								Model uimodel,
								HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			response.setStatus(405);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return "testfile";
		}
		//model.SetLogs("/Video/Callanswer?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token)+"&status="+status);
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" ") && !status.isEmpty() && !status.contains(" "))
		{
			model.SetCallAnswer( usernamefrom,username,status,counttoicecandidates);
			uimodel.addAttribute("name","Success");
		}
		else {
			if(!model.GetDbStatus()) {
				uimodel.addAttribute("name","Error db status");
			} else {
				uimodel.addAttribute("name","You are not authorized");
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return "testfile";
	}

	@GetMapping("/Video/GetCallAnswer")
	@ResponseBody
	public HashMap<String, Object> GetCallAnswer(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
												 @RequestParam(name="username", required=false, defaultValue= "") String username,
												 @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												 HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetCallanswer?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> callAnswer = model.GetCallAnswer( usernamefrom,username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("StatusCall", callAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/MyCallRequest")
	@ResponseBody
	public HashMap<String, Object> MyCallRequest(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
												 @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
												 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
												 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
												 HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/MyCallrequest?Token="+token+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty())
		{
			setStatus(200,response,jsonAnswer);
			jsonAnswer.put("Request",model.GetCallRequest( usernamefrom));
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetFromIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetFromIceCandidates(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
														@RequestParam(name="username", required=false, defaultValue= "") String username,
														@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
														@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
														@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
														HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetFromIceCandidates?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> iceCandidatesAnswer = model.GetFromIceCandidates(usernamefrom,username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("fromicecandidates",iceCandidatesAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetCountToIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetCountToIceCandidates(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
														   @RequestParam(name="username", required=false, defaultValue= "") String username,
														   @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
														   @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
														   @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
														   HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		GetCountIceCandidates(usernamefrom,response,"counttoicecandidates", jsonAnswer,serviceToken, serviceSalt, serviceTime  );
		return jsonAnswer;
	}

	@GetMapping("/Video/GetCountFromIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetCountFromIceCandidates(@RequestParam(name="username", required=false, defaultValue= "") String username,
															 @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
															 @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
															 @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
															 HttpServletResponse response) {
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		GetCountIceCandidates(username, response,"countfromicecandidates",jsonAnswer,serviceToken, serviceSalt, serviceTime );
		return jsonAnswer;
	}


	private HashMap<String, Object> GetCountIceCandidates(String username, HttpServletResponse response,String nameField, HashMap<String, Object> jsonAnswer,
														  String serviceToken, String serviceSalt, String serviceTime)
	{
		WebRTCModel model = new WebRTCModel();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetCountIceCandidates?username="+username);
		if(!username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			String countIceCandidates = model.GetStringFromTable(nameField,"Speakstars.CallRequest","WHERE fromuser='"+username+"'");
			if(model.GetQueryStatus()) {
				jsonAnswer.put(nameField,countIceCandidates);
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetToIceCandidates")
	@ResponseBody
	public HashMap<String, Object> GetToIceCandidates(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
													  @RequestParam(name="username", required=false, defaultValue= "") String username,
													  @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													  HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetToIceCandidates?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> iceCandidatesAnswer = model.GetToIceCandidates( usernamefrom,username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("toicecandidates",iceCandidatesAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetFromDescription")
	@ResponseBody
	public HashMap<String, Object> GetFromDescription(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
													  @RequestParam(name="username", required=false, defaultValue= "") String username,
													  @RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													  @RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													  @RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													  HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetFromDescription?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> fromDescriptionAnswer = model.GetFromDescription( usernamefrom,username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("fromdescription",fromDescriptionAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

	@GetMapping("/Video/GetToDescription")
	@ResponseBody
	public HashMap<String, Object> GetToDescription(@RequestParam(name="usernamefrom", defaultValue="") String usernamefrom,
													@RequestParam(name="username", required=false, defaultValue= "") String username,
													@RequestParam(name="serviceToken", defaultValue = "-1") String serviceToken,
													@RequestParam(name="serviceSalt", defaultValue = "-1") String serviceSalt,
													@RequestParam(name="serviceTime", defaultValue = "-1") String serviceTime,
													HttpServletResponse response) {
		WebRTCModel model = new WebRTCModel();
		HashMap<String, Object> jsonAnswer = new LinkedHashMap<String, Object>();
		if(model.checkServiceToken(serviceToken,serviceSalt,serviceTime))
		{
			setStatus(405,response,jsonAnswer);
			try {
				model.finalize();
			} catch (SQLException exc) {}
			return jsonAnswer;
		}
		//model.SetLogs("/Video/GetFromDescription?Token="+token+"&username="+username+"&usernamefrom="+model.GetUsername(token));
		if(!usernamefrom.isEmpty() && !username.isEmpty() && !username.contains(" "))
		{
			setStatus(200,response,jsonAnswer);
			List<String> toDescriptionAnswer = model.GetToDescription( usernamefrom,username);
			if(model.GetQueryStatus()) {
				jsonAnswer.put("todescription",toDescriptionAnswer.get(0));
			} else {
				if(!model.GetDbStatus()) {
					setStatus(500,response,jsonAnswer);
				} else {
					setStatus(401,response,jsonAnswer);
				}
			}
		}
		else {
			if(!model.GetDbStatus()) {
				setStatus(500,response,jsonAnswer);
			} else {
				setStatus(401,response,jsonAnswer);
			}
		}
		try {
			model.finalize();
		} catch (SQLException exc) {}
		return jsonAnswer;
	}

    @PostMapping("/GetServiceToken")
    @ResponseBody
    public HashMap<String, String> getServiceToken(@RequestBody String password) {
        WebRTCModel model = new WebRTCModel();
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
		WebRTCModel model = new WebRTCModel();
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