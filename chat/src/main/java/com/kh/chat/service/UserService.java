package com.kh.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.chat.mapper.UserMapper;
import com.kh.chat.model.dto.UserDTO;

@Service
public class UserService {
	@Autowired
	private UserMapper mapper;
	
	public boolean join(UserDTO user) {
		return mapper.insertUser(user) == 1;
	}

	public boolean login(UserDTO user) {
		UserDTO temp = mapper.getUserById(user.getUserid());
		if(temp != null) {
			if(temp.getUserpw().equals(user.getUserpw())) {
				return true;
			}
		}
		return false;
	}
}
