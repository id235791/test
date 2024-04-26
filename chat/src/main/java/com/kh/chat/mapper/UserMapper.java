package com.kh.chat.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kh.chat.model.dto.UserDTO;

@Mapper
public interface UserMapper {
	public int insertUser(UserDTO user);

	public UserDTO getUserById(String userid);
}
