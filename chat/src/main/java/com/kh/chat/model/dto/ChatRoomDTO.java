package com.kh.chat.model.dto;

import java.util.Set;

import lombok.Data;

@Data
public class ChatRoomDTO {
	private String roomnum;
	private String title;
	private String regdate;
	private Set<String> members;
}
