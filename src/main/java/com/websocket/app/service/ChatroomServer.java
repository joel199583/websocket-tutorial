package com.websocket.app.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.app.bean.ChatBean;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component

/*
*@ServerEndpoint 註解是一個類層次的註解，它的功能主要是將目前的類定義成一個websocket伺服器端,
* 客戶端可以通過這個URL來連線到WebSocket伺服器端
*/
@ServerEndpoint(value = "/chatroom/{name}")
public class ChatroomServer {


    //用来紀錄當前再線人數。應注意需是執行緒安全的。
    private static AtomicInteger online = new AtomicInteger();

    //concurrent包的執行緒安全Map，用来存放每個客户端對應的連線。websocket container
    private static Map<String, Session> sessionPools = new ConcurrentHashMap<>();
    
    /**
     * 連線建立成功呼叫的方法
     * @param session 客戶端與伺服器端建立的連線
     * @param userName 客户端的userName (識別碼)
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "name") String userName) throws Exception{
        sessionPools.put(userName, session);
        addOnlineCount();
        System.out.println(userName + "加入webSocket！當前人數為" + online);
		sessionPools.forEach((k, otherSession) -> {
			
	
			ChatBean bean = new ChatBean();
			bean.setUsers(sessionPools.keySet()
									  .stream()
									  .filter(s -> !s.equals(k))
									  .collect(Collectors.toSet()));
			bean.setType("open");
			bean.setMsg("歡迎 " + userName + " 加入聊天！");
			sendMessage(otherSession, bean);	
		});
    }

    /**
     * 連線關閉呼叫的方法
     * @param userName 關閉連線的userName
     */
    @OnClose
    public void onClose(@PathParam(value = "name") String userName){
        sessionPools.remove(userName);
        subOnlineCount();
        System.out.println(userName + "關閉webSocket連線！當前人数為" + online);
        sessionPools.forEach((k, session) -> {
			ChatBean bean = new ChatBean();
			bean.setUsers(sessionPools.keySet()
									  .stream()
									  .filter(s -> !s.equals(k))
									  .collect(Collectors.toSet()));
			bean.setType("close");
			bean.setMsg(userName + " 離開聊天！");
			sendMessage(session, bean);
		});
    }

    /**
     * 收到客户端消息時觸發
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(@PathParam(value = "name") String fromUser, String jsonString) throws IOException{
    	ChatBean chatContent = new ObjectMapper().readValue(jsonString, ChatBean.class);
    	
    	if("all".equals(chatContent.getTo())) {
    		sessionPools.forEach((key, session) -> {
    			ChatBean rtnBean = new ChatBean();
    			rtnBean.setType("all");
    			rtnBean.setFrom(fromUser);
    			rtnBean.setMsg(chatContent.getMsg());
    			sendMessage(session, rtnBean);
            });
    	} else {
    		ChatBean rtnBean = new ChatBean();
			rtnBean.setType("dm");
			rtnBean.setFrom(fromUser);
			rtnBean.setMsg(chatContent.getMsg());
			//如果被私訊者還在線上
			if(sessionPools.containsKey(chatContent.getTo())) {
				sendMessage(sessionPools.get(chatContent.getTo()), rtnBean); //推給你私訊的人
				sendMessage(sessionPools.get(fromUser), rtnBean); //推給自己
			} else {
				rtnBean.setType("miss");
				rtnBean.setMsg(chatContent.getTo() + " 已離線");
				sendMessage(sessionPools.get(fromUser), rtnBean);
			}
    	}
    }

    /**
     * 發生異常時觸發
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable){
        System.out.println("發生錯誤");
        throwable.printStackTrace();
    }
    
    /**
     * 發送訊息
     * @param session 客戶端與伺服器端建立的連線
     * @param message 訊息
     * @throws IOException
     */
    public void sendMessage(Session session, String message) {
        if(session != null){
        	session.getAsyncRemote().sendText(message);;
        }
    }
    
    /**
     * 發送訊息(傳送內容為 自訂義物件 ChatBean)
     * @param session 客戶端與伺服器端建立的連線
     * @param chatBean 回傳內容
     * @throws IOException
     */
    public void sendMessage(Session session, ChatBean chatBean) {
    	if(session != null){
        	try {
        		session.getAsyncRemote().sendText(new ObjectMapper().writeValueAsString(chatBean));;
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 给指定用户發送消息
     * @param userName 用户名
     * @param message 消息
     * @throws IOException
     */
    public void sendInfo(String fromUser, String userName, String message){
        Session session = sessionPools.get(userName);
        try {
            sendMessage(session, "來自 " + userName + " 的訊息:" + message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void addOnlineCount(){
        online.incrementAndGet();
    }

    public static void subOnlineCount() {
        online.decrementAndGet();
    }
}