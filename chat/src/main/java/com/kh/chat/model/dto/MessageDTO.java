package com.kh.chat.model.dto;

import lombok.Data;

@Data
public class MessageDTO {
	public enum Type{
		GLOBAL, LIST, CREATE, ENTER, NORMAL, EXIT
	}
	private Type type;
	private String content;
	private String time;
	private String userid;
	private String roomnum;
}
