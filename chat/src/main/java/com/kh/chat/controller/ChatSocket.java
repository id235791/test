package com.kh.chat.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kh.chat.model.dto.ChatRoomDTO;
import com.kh.chat.model.dto.MessageDTO;
import com.kh.chat.model.dto.MessageDTO.Type;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value="/ws/{userid}")
@Service
public class ChatSocket {
	private static List<ChatRoomDTO> rooms = Collections.synchronizedList(new ArrayList<>()); 
	private static Map<String,Session> clients_id = Collections.synchronizedMap(new HashMap<>());
	
	@OnMessage
	public void onMessage(String message, Session session) throws Exception{
		Gson gson = new Gson();
		JsonObject json = new JsonObject();
		MessageDTO mdto = gson.fromJson(message, MessageDTO.class);
		synchronized (clients_id) {
			Set<Entry<String, Session>> entryset = clients_id.entrySet();
			Collection<Session> clients = clients_id.values();
			String loginUser = null;
			for(Entry<String, Session> entry : entryset) {
				if(entry.getValue().equals(session)) {
					loginUser = entry.getKey();
					break;
				}
			}
			Type type = mdto.getType();
			if(type.equals(Type.GLOBAL)) {
				System.out.println("Global To "+clients_id.get(session));
				json.add("rooms", gson.toJsonTree(rooms));
				session.getBasicRemote().sendText("global:"+json.toString());
			}
			else if(type.equals(Type.CREATE)) {
				String title = mdto.getContent();
				System.out.println("Create room : "+title);
				ChatRoomDTO newRoom = new ChatRoomDTO();
				newRoom.setMembers(new HashSet<>());
				
				newRoom.getMembers().add(loginUser);
				Date now = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				newRoom.setRegdate(sdf.format(now));
				newRoom.setRoomnum(now.getTime()+loginUser);
				newRoom.setTitle(title);
				synchronized (rooms) {
					rooms.add(newRoom);
				}
				json.add("rooms", gson.toJsonTree(rooms));
				for(Session client : clients) {
					if(!client.equals(session)) {
						client.getBasicRemote().sendText("global:"+json.toString());
					}
				}
				session.getBasicRemote().sendText("create:"+newRoom.getRoomnum());
			}
			else if(type.equals(Type.ENTER)){
				System.out.println(mdto);
				synchronized (rooms) {
					Set<String> members = null;
					for(ChatRoomDTO room : rooms) {
						String roomnum = room.getRoomnum();
						if(roomnum.equals(mdto.getRoomnum())) {
							members = room.getMembers();
							break;
						}
					}
					int before = members.size();
					members.add(loginUser);
					int after = members.size();
					System.out.println("before : "+before);
					System.out.println("after : "+after);
					if(after != before) {
						json.addProperty("roomnum", mdto.getRoomnum());
						json.addProperty("userid", mdto.getUserid());
						JsonObject json2 = new JsonObject();
						json2.add("rooms", gson.toJsonTree(rooms));
						for(String userid : members) {
							clients_id.get(userid).getBasicRemote().sendText("in:"+json.toString());
						}
						for(Session client : clients) {
							client.getBasicRemote().sendText("global:"+json2.toString());
						}
					}
					
				}
			}
			else if(type.equals(Type.NORMAL)) {
				synchronized(rooms) {
					json.add("data", gson.toJsonTree(mdto));
					Set<String> members = null;
					for(ChatRoomDTO room : rooms) {
						if(room.getRoomnum().equals(mdto.getRoomnum())) {
							members = room.getMembers();
							break;
						}
					}
					for(String userid : members) {
						clients_id.get(userid).getBasicRemote().sendText("normal:"+json.toString());
					}
				}
			}
		}
	}
	@OnOpen
	public void onOpen(Session session, @PathParam(value="userid") String userid) throws Exception{
		System.out.println("접속 : "+userid);
		Gson gson = new Gson();
		JsonObject json = new JsonObject();
		clients_id.put(userid, session);
		json.add("rooms", gson.toJsonTree(rooms));
		session.getBasicRemote().sendText("global:"+json.toString());
	}
	@OnClose
	public void onClose(Session session) throws Exception{
		Gson gson = new Gson();
		String outUser = null;
		synchronized (clients_id) {
			Set<Entry<String,Session>> entryset = clients_id.entrySet();
			for(Entry<String, Session> entry : entryset) {
				if(entry.getValue().equals(session)) {
					outUser = entry.getKey();
					break;
				}
			}
			System.out.println("종료 : "+outUser);
			ArrayList<ChatRoomDTO> deletedRoom = new ArrayList<>();
			synchronized (rooms) {
				//모든 채팅방 순회
				for(ChatRoomDTO room : rooms) {
					//채팅방 참여자들 아이디
					Set<String> members = room.getMembers();
					if(members.contains(outUser)) {
						JsonObject json = new JsonObject();
						json.addProperty("roomnum", room.getRoomnum());
						json.addProperty("userid", outUser);
						for(String userid : members) {
							if(userid.equals(outUser)) {
								continue;
							}
							clients_id.get(userid).getBasicRemote().sendText("out:"+json.toString());
						}
						//참여자에서 퇴장한 아이디 삭제
						members.remove(outUser);
					}
					//채팅방 남은 사람이 없다면
					if(members.size() == 0) {
						deletedRoom.add(room);
					}
				}
				//전부 삭제
				rooms.removeAll(deletedRoom);
			}
			//접속자 명단에서 삭제
			clients_id.remove(outUser);
			entryset = clients_id.entrySet();
			JsonObject json = new JsonObject();
			json.add("rooms", gson.toJsonTree(rooms));
			for(Entry<String, Session> entry : entryset) {
				entry.getValue().getBasicRemote().sendText("global:"+json.toString());
			}
		}
	}
}
