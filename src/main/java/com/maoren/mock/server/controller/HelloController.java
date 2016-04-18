package com.maoren.mock.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/server1/1.0")
public class HelloController {
	
	@RequestMapping("/hello")
	@ResponseBody  
	public String hello(){
		return "hello world;";
	}
	
	@RequestMapping("/world")
	@ResponseBody  
	public String hello1(){
		return "hello world;";
	}

}
