package com.kh.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.chat.model.dto.UserDTO;
import com.kh.chat.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ChatController {
	@Autowired
	private UserService uservice;
	@GetMapping("/")
	public String home() {
		System.out.println("/");
		return "index";
	}
	@GetMapping(value={"/join","/chat"})
	public void replace() {
		System.out.println("replace");
	}
	@PostMapping(value="/join",consumes = "application/json")
	@ResponseBody
	public String join(@RequestBody UserDTO user) {
		System.out.println("/join");
		if(uservice.join(user)) {
			return "O";
		}
		else {
			return "X";
		}
	}
	@GetMapping("/login")
	@ResponseBody
	public String login(@ModelAttribute UserDTO user,HttpServletRequest req) {
		System.out.println("/login");
		if(uservice.login(user)) {
			HttpSession session = req.getSession();
			session.setAttribute("loginUser", user.getUserid());
			return "O";
		}
		else {
			return "X";
		}
	}
	@GetMapping("/logout")
	@ResponseBody
	public String logout(HttpServletRequest req) {
		System.out.println("/logout");
		System.out.println("id235791");
		HttpSession session = req.getSession();
		session.removeAttribute("loginUser");
		return "O";
	}
	
}
