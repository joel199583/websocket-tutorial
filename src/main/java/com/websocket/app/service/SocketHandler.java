package com.websocket.app.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class SocketHandler implements WebSocketHandler {
	
	private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String userName = session.getAttributes().get("userName").toString();
		SESSIONS.put(userName, session);
		System.out.println(String.format("Welcome %s", userName));
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		String msg = message.getPayload().toString();
		System.out.println(msg);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.out.println("连接出错");
		if (session.isOpen()) {
			session.close();
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		SESSIONS.remove(session.getUri().getQuery().replace("name=", ""));
		System.out.println("连接已關閉,status:" + closeStatus);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 指定发消息
	 *
	 * @param message
	 */
	public static void sendMessage(String userName, String message) {
		WebSocketSession webSocketSession = SESSIONS.get(userName);
		if (webSocketSession == null || !webSocketSession.isOpen())
			return;
		try {
			webSocketSession.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Scheduled(fixedRate = 2000) // fixedRate = 2000 表示當前方法開始執行 2000ms(2秒鐘) 後，Spring scheduling會再次呼叫該方法
    public static void serverPushNotification() {
		System.out.println("現在時間" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
		SESSIONS.forEach((k, session) -> {
        	sendMessage(k, "Hi " + k + " 現在時間 : " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        });
    }
	
	/**
	 * 群发消息
	 *
	 * @param message
	 */
	public static void fanoutMessage(String message) {
		SESSIONS.keySet().forEach(us -> sendMessage(us, message));
	}
}
