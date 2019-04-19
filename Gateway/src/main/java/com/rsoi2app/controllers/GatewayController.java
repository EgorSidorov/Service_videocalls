package com.rsoi2app.controllers;

import com.rsoi2app.config.Startup;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.parser.Parser;
import java.io.IOException;
import java.util.*;

@Controller
public class GatewayController {

	@PostMapping("/login")
	public String login(Model model,
						HttpServletRequest request,
						HttpServletResponse response) throws IOException, ParseException {
		Map<String,String[]> body = request.getParameterMap();
		String[] username = body.get("username");
		String[] password = body.get("password");
		String status = "";
		String appID = "";
		if(body.containsKey("action"))
			status = body.get("action")[0];
		if(body.containsKey("appID"))
			appID = body.get("appID")[0];
		if(!username[0].isEmpty() && !password[0].isEmpty() && !username[0].contains(" ") && !password[0].contains(" "))
		{
			String responseStr1 = Utils.requestPostForService(Startup.GetAccountService()+"/Login?username="+username[0],password[0],"none",Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").toString().equals("Success")) {
					response.addCookie(new Cookie("Token", userJson.get("Cookie").toString()));
					if(status.equals("oauth"))
						return "redirect:https://speakstars.ru/authorization?appID="+appID;
					else return startRedirect();
				}
				else {
					response.setStatus(401);
					return "redirect:https://speakstars.ru/login";
				}
			}
			else {
				response.setStatus(401);
				return "redirect:https://speakstars.ru/login";
			}


		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "authorize";
	}

	@GetMapping("/login")
	public String getLogin(Model model,
						   @RequestParam(name="status", required=false, defaultValue= "") String status,
						   @RequestParam(name="appID", required=false, defaultValue= "") String appID){
		if(status.equals("oauth")) {
			model.addAttribute("applID",appID);
			return "authorizeoauth";
		}
		else return "authorize";
	}

	@GetMapping("/")
	public String startRedirect()
	{
		return "redirect:https://speakstars.ru/payment";
	}

	@GetMapping("/logout")
	public String logout(@CookieValue(name="Token", defaultValue= "") String token) throws IOException, ParseException {
		Utils.requestForService(Startup.GetAccountService()+"/Logout",token,Utils.Services.Account);
		return "authorize";
	}

	@GetMapping("/call")
	public String call(@CookieValue(name="Token", defaultValue= "") String token,
						 Model model,
						 HttpServletResponse response) throws IOException, ParseException {
		Utils.UserInfo info = Utils.GetUserInfo(token);
		if(info.getStatus() && info.getLogged()) {
			String usernameFrom = info.getUsername();
			model.addAttribute("username",usernameFrom);
		String responseStr = Utils.requestForService(Startup.GetWebrtcService()+"/PhonebookList"+"?usernamefrom="+usernameFrom,"none",Utils.Services.WebRTC);
		if (!responseStr.contains("Error:")) {
			JSONParser parser = new JSONParser();
			JSONObject userJson = (JSONObject) parser.parse(responseStr);
			if (userJson.get("Status").toString().equals("Success")) {
						String responseStr3 = Utils.requestForService(Startup.GetPaymentService() + "/Show_cash?username=" + usernameFrom, token,Utils.Services.Payment);
						if (!responseStr3.contains("Error:")) {
							JSONParser parser3 = new JSONParser();
							JSONObject userJson3 = (JSONObject) parser3.parse(responseStr3);
							if (userJson3.get("Status").equals("Success")) {
								if(Integer.parseInt(userJson3.getAsString("cash"))>0)
									model.addAttribute("alllist",userJson.get("UserList"));
								else
								{
									model.addAttribute("name","Пополните счет");
									response.setStatus(401);
									return "greeting";
								}
							}
						} else {
							model.addAttribute("name","Ошибка получения счета");
							response.setStatus(401);
							return "greeting";
						}
					}
				}
			}
		else {
			response.setStatus(500);
			return "redirect:https://speakstars.ru/login";
		}
		return "callrequest";
	}

	@GetMapping("/historycall")
	public String historycall(
			@RequestParam(name="page", required=false, defaultValue= "0") String page,
			@CookieValue(name="Token", defaultValue= "") String token,
			Model model,
			HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !page.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				} else return "redirect:https://speakstars.ru/login";
			}
			if(status1) {
				String responseStr2 = Utils.requestForService(Startup.GetCallsService() + "/Show?username=" + username+"&page="+page, "none",Utils.Services.Calls);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").toString().equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2) {
				List<String> listjson = (List<String>)userJson.get("History");
				List<String> datestring = new ArrayList<>();
				List<String> typestring = new ArrayList<>();
				List<String> namestring = new ArrayList<>();
				List<String> durationstring = new ArrayList<>();
				List<Integer> historystringiter = new ArrayList<>();
				for(int i = 0; i < listjson.size(); i++)
				{
					String[] elements = listjson.get(i).split(";");
					datestring.add(elements[0]);
					if(elements[1].equals("1"))
						typestring.add("Входящий");
					else if(elements[1].equals("2"))
						typestring.add("Исходящий");
					else
						typestring.add("NULL");

					namestring.add(elements[2]);
					durationstring.add(elements[3]);
					historystringiter.add(i);
				}
				model.addAttribute("datestring", datestring);
				model.addAttribute("typestring", typestring);
				model.addAttribute("namestring", namestring);
				model.addAttribute("durationstring", durationstring);
				model.addAttribute("historystringiter", historystringiter);
				model.addAttribute("username", username);
				int intPage = Integer.parseInt(page);
				if(intPage>0) {
					model.addAttribute("page", String.valueOf(intPage - 1));
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				} else {
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				}
			}
		}
		else {
			response.setStatus(400);
			return "redirect:https://speakstars.ru/login";
		}
		return "historycall";
	}

	@RequestMapping(value = "/phonebook",  method = {RequestMethod.POST, RequestMethod.GET})
	public String phonebook(
			@RequestParam(name="page", required=false, defaultValue= "0") String page,
			@RequestParam(name="username", required=false, defaultValue= "0") String inputusername,
			@RequestBody(required = false) String accessToken,
			@RequestParam(name = "appId", required = false, defaultValue = "-1") String appId,
			@RequestParam(name="status", required=false, defaultValue= "0") String status,
			@CookieValue(name="Token", defaultValue= "") String token,
			Model model,
			HttpServletResponse response) throws IOException, ParseException {
		if(appId.equals("-1"))
			accessToken = "";
		Utils.UserInfo info;
		String username = "";
		if((!token.isEmpty() && !token.contains(" ") && !page.contains(" ")) || !appId.equals("-1"))
		{
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			Boolean statuss = false;
			Boolean isLogged = false;
			if(appId.equals("-1")) {
			info = Utils.GetUserInfo(token);
				statuss = info.getStatus();
				isLogged = info.getLogged();
				username = info.getUsername();
			} else token = "none";
			if((!statuss || !isLogged) && appId.equals("-1")) {
				return "redirect:https://speakstars.ru/login";
			}
				String responseStr2 = Utils.requestPostForService(Startup.GetAccountService() + "/UsersList?page="+page+
						"&appId="+appId+"&username="+inputusername,accessToken, token,Utils.Services.Account);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").toString().equals("Success")) {
						status2 = true;
					}
				}
			if(status2) {
				List<String> listjson = (List<String>)userJson.get("UserList");
				List<String> namestring = new ArrayList<>();
				List<Integer> historystringiter = new ArrayList<>();
				for(int i = 0; i < listjson.size(); i++)
				{
					//String[] elements = listjson.get(i).split(";");
					namestring.add(listjson.get(i));
					historystringiter.add(i);
				}
				model.addAttribute("namestring", namestring);
				model.addAttribute("historystringiter", historystringiter);
				model.addAttribute("username", username);
				int intPage = Integer.parseInt(page);
				if(intPage>0) {
					model.addAttribute("page", String.valueOf(intPage - 1));
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				} else {
					model.addAttribute("page2", String.valueOf(intPage));
					model.addAttribute("page3", String.valueOf(intPage + 1));
				}
			}
		}
		else {
			response.setStatus(400);
			return "redirect:https://speakstars.ru/login";
		}
		if(status.equals("action"))
		{
			Utils.requestForService(Startup.GetWebrtcService()+"/Add_phonebook?username="+inputusername+"&usernamefrom="+username,"none",Utils.Services.WebRTC);
		}
		model.addAttribute("username", username);
		return "phonebook";

	}

	private boolean isNumber(String str) {
		if (str == null || str.isEmpty()) return false;
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.toCharArray()[i])) return false;
		}
		return true;
	}

	@GetMapping("/payment")
	public String showCash(@CookieValue(name="Token", defaultValue= "") String token,
						   @RequestParam(name="coins", required=false, defaultValue= "") String cash,
						Model model,
						HttpServletResponse response) throws IOException, ParseException, InterruptedException {

		if(!token.isEmpty() && !token.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			String statusmessage = "";
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
					if(isNumber(cash)) {
						String responcestr = Utils.requestForService(Startup.GetPaymentService() + "/Add_cash?username=" + username + "&cash=" + cash, "none", Utils.Services.Payment);
						statusmessage = "Добавлено";
						if(responcestr.contains("Error:Timeout") )
							BlockingQueueWorker.getInstance().setQuery(Startup.GetPaymentService() + "/Add_cash?username=" + username+"&cash="+cash,Utils.Services.Payment);
					} else if(!cash.isEmpty()){
						statusmessage = "Необходимо ввести число";
					}

				}  else return "redirect:https://speakstars.ru/login";
			}
			if(status1) {
				String responseStr2 = Utils.requestForService(Startup.GetPaymentService() + "/Show_cash?username=" + username, "none",Utils.Services.Payment);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("cash", userJson.getAsString("cash"));
			if(!statusmessage.isEmpty())
				model.addAttribute("message",statusmessage);
			if(status1)
				model.addAttribute("username", username);
		}
		else {
			response.setStatus(400);
		}
		return "payment";
	}

	@PostMapping("/registration")
	public String register(Model model,
						HttpServletRequest request,
						HttpServletResponse response) throws IOException, ParseException {
		Map<String,String[]> body = request.getParameterMap();
		if(!body.get("status")[0].equals("action"))
			return "register";
		if(!body.get("username")[0].isEmpty() && !body.get("password")[0].isEmpty() && !body.get("username")[0].contains(" ") && !body.get("password")[0].contains(" "))
		{
			String responseStr1 = Utils.requestPostForService(Startup.GetAccountService()+"/Create?username="+body.get("username")[0],body.get("password")[0],"none",Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").toString().equals("Success")) {
				}
			} else {
				model.addAttribute("name","Error create user.");
			}
			String responseStr2 = Utils.requestForService(Startup.GetPaymentService()+"/New_purse?username="+body.get("username")[0],"none",Utils.Services.Payment);
			if (!responseStr2.contains("Error:")) {
				JSONParser parser = new JSONParser();
				JSONObject userJson = (JSONObject) parser.parse(responseStr2);
				if(userJson.get("Status").toString().equals("Success")) {
					return "redirect:https://speakstars.ru/login";
				}
			} else
			{
				Utils.requestPostForService(Startup.GetAccountService()+"/Delete?username="+body.get("username")[0],body.get("password")[0],"none",Utils.Services.Account);
				model.addAttribute("name","Error create purse. Operation rollback.");
			}
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/registration")
	public String getRegister(){
		return "register";
	}


	@GetMapping("/withdraw")
	public String withdrawCash(@CookieValue(name="Token", defaultValue= "") String token,
							   @RequestParam(name="cash", required=false, defaultValue= "") String cash,
							   Model model,
							   HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !cash.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}  else return "redirect:https://speakstars.ru/login";
			}
			if(status1) {
				String responseStr2 = Utils.requestForService(Startup.GetPaymentService()+"/Withdraw_cash?username=" + username+"&cash="+cash, "none",Utils.Services.Payment);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("name","Success withdraw");
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/addcall")
	public String addCall(@CookieValue(name="Token", defaultValue= "") String token,
						  @RequestParam(name="duration", required=false, defaultValue= "") String duration,
						  @RequestParam(name="usernameto", required=false, defaultValue= "") String usernameto,
						  @RequestParam(name="datetime", required=false, defaultValue= "") String datetime,
						  Model model,
						  HttpServletResponse response) throws IOException, ParseException {
		if(!token.isEmpty() && !token.contains(" ") && !duration.contains(" "))
		{
			boolean status1 = false;
			boolean status2 = false;
			JSONObject userJson = new JSONObject();
			String username = "";
			String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
			if (!responseStr1.contains("Error:")) {
				JSONParser parser = new JSONParser();
				userJson = (JSONObject) parser.parse(responseStr1);
				if(userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
					username = userJson.get("username").toString();
					status1 = true;
				}  else return "redirect:https://speakstars.ru/login";
			}
			if(status1) {
				String responseStr2 = Utils.requestForService(Startup.GetCallsService() + "/New?username=" + username+"&duration="+duration+"&usernameto="+usernameto+"&datetime="+datetime, "none",Utils.Services.Calls);
				if (!responseStr2.contains("Error:")) {
					JSONParser parser = new JSONParser();
					userJson = (JSONObject) parser.parse(responseStr2);
					if (userJson.get("Status").equals("Success")) {
						status2 = true;
					}
				}
			}
			if(status2)
				model.addAttribute("name","Success add");
			else
				model.addAttribute("name","Error token");
		}
		else {
			model.addAttribute("name","Error parameters");
			response.setStatus(400);
		}
		return "greeting";
	}

	@GetMapping("/authorization")
	public String grantRules(@RequestParam(name="appID", defaultValue= "") String appID,
							 @RequestParam(name="scope",required = false, defaultValue = "all") String scope,
							 @RequestParam(name="status",required = false, defaultValue = "") String action,
							 Model model,
							 @CookieValue(name="Token", defaultValue="") String token) throws IOException, ParseException {
		model.addAttribute("appId",appID);
		model.addAttribute("scope",scope);
		JSONObject userJson = new JSONObject();
		String username = "";
		String responseStr1 = Utils.requestForService(Startup.GetAccountService()+"/UserInfo",token,Utils.Services.Account);
		if (!responseStr1.contains("Error:")) {
			JSONParser parser = new JSONParser();
			userJson = (JSONObject) parser.parse(responseStr1);
			if (userJson.get("Status").equals("Success") && userJson.get("IsLogged").toString().equals("true")) {
				username = userJson.get("username").toString();
			} else return "redirect:https://speakstars.ru/login?status=oauth&appID="+appID;
		} else return "redirect:https://speakstars.ru/login?status=oauth&appID="+appID;
		model.addAttribute("username",username);
		if(action.equals("accept")) {
			String responce = Utils.requestForService(Startup.GetAccountService() + "/grantRules?appID=" + appID, token, Utils.Services.Account);
			JSONParser parser2 = new JSONParser();
			JSONObject userJson2 = (JSONObject) parser2.parse(responce);
			if (userJson2.getAsString("Status").equals("Success")) {
				model.addAttribute("scope",userJson2.getAsString("scope"));
				return "redirect:" + userJson2.getAsString("redirectURI") + "?Username=" + userJson2.getAsString("Username") + "&authtoken=" + userJson2.getAsString("Auth token");
			}
			else
				return "redirect:https://speakstars.ru/login?status=oauth&appID="+appID;
		} else if(action.equals("refused")){
			return "redirect:https://speakstars.ru/payment";
		} else return "oauthredirect";
	}

	@PostMapping("/accessToken")
	@ResponseBody
	public String accessToken(@RequestParam(name="appID", defaultValue= "-1") String appID,
							  @RequestParam(name="username", defaultValue= "-1") String username,
							  @RequestParam(name="authToken", defaultValue= "-1") String authToken,
							  HttpServletRequest request) throws IOException, ParseException {
		Map<String,String[]> body = request.getParameterMap();
		return Utils.requestPostForService(Startup.GetAccountService()+"/accessToken?appID="+appID+"&username="+username+"&authToken="+authToken,body.get("secret")[0],"none",Utils.Services.Account);
	}

	@PostMapping("/refreshToken")
	@ResponseBody
	public String refreshToken(@RequestParam(name="appID", defaultValue= "-1") String appID,
							   @RequestParam(name="username", defaultValue= "-1") String username,
							   @RequestParam(name="refreshToken", defaultValue= "-1") String oldrefreshToken,
							   HttpServletRequest request) throws IOException, ParseException {
		Map<String,String[]> body = request.getParameterMap();
		return Utils.requestPostForService(Startup.GetAccountService() + "/refreshToken?appID=" + appID + "&username=" + username + "&refreshToken=" + oldrefreshToken,body.get("secret")[0], "none", Utils.Services.Account);
	}
}