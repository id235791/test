class WebSocketService {
	constructor(userid) {
	    this.socket = new WebSocket("ws://localhost:8080/ws/"+userid);
	    this.socket.onerror = this.onError;
	    this.socket.onopen = this.onOpen;
	    this.socket.onmessage = this.onMessage;
	    this.socket.onclose = this.onClose;
	}

	onError(e) {
		console.error("WebSocket error:", e);
	}

	onOpen(e) {
		console.log("WebSocket connection opened");
	}

	onMessage(e) {
		let idx = e.data.indexOf(":");
		const command = e.data.substring(0,idx);
		let data = e.data.substring(idx+1);
		let str = "";
		console.log(data);
		if(command == "global"){
			data = JSON.parse(data);
			const rooms = data.rooms;
			displayRooms(rooms);
		}
		else if(command == "create"){
			socket.send(JSON.stringify({type:"ENTER",roomnum:data,userid:loginUser}));
			enterRoom(data)
		}
		else if(command == "in"){
			data = JSON.parse(data);
			if(enteredRoomNum == data.roomnum){
				str += `<div class="inout-msg">${data.userid}님이 입장하셨습니다.</div>`
				$(chatLog).append(str)
				chatLog.scrollTop = chatLog.scrollHeight;
			}
			
		}
		else if(command == "out"){
			data = JSON.parse(data);
			if(enteredRoomNum == data.roomnum){
				str += `<div class="inout-msg">${data.userid}님이 퇴장하셨습니다.</div>`
				$(chatLog).append(str)
				chatLog.scrollTop = chatLog.scrollHeight;
			}
		}
		else if(command == "normal"){
			data = JSON.parse(data);
			data = data.data;
			if(enteredRoomNum == data.roomnum){
				str += `<div class="chat-content ${data.userid == loginUser?'self':'user'}">
	<div class="msg">${data.content}</div>
	<div class="sender">${data.userid == loginUser?'모두에게':data.userid}이(가)</div>
</div>`
				$(chatLog).append(str);
				chatLog.scrollTop = chatLog.scrollHeight;
			}
		}
	}

	onClose(e) {
		console.log("WebSocket connection closed");
	}

	// 여기에 필요한 다른 메서드를 추가할 수 있습니다.

	// 싱글톤 인스턴스 반환
	static getInstance(userid) {
		if (!this.instance) {
			this.instance = new WebSocketService(userid);
		}
		return this.instance;
	}
}
