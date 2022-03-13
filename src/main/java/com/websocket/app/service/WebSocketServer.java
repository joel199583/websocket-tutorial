package com.websocket.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
/*
* @ServerEndpoint 註解是一個類層次的註解，它的功能主要是將目前的類定義成一個websocket伺服器端,
* 客戶端可以通過這個URL來連線到WebSocket伺服器端
*/
@ServerEndpoint(value = "/socket/{name}")
public class WebSocketServer {

    //concurrent包的執行續安全Map，用来存放每個客户端對應的連線。websocket 容器
    private static Map<String, Session> sessionPools = new ConcurrentHashMap<>();
    
    /**
     * 連線建立成功呼叫的方法
     * @param session 客戶端與伺服器端建立的連線
     * @param userName 客户端的userName (識別碼)
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "name") String userName){
        sessionPools.put(userName, session);
    }

    /**
     * 連線關閉呼叫的方法
     * @param userName 關閉連線的userName
     */
    @OnClose
    public void onClose(@PathParam(value = "name") String userName){
        sessionPools.remove(userName);
    }

    /**
     * 收到客户端消息時觸發（群發）
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(@PathParam(value = "name") String userName, String message) throws IOException{
    	sessionPools.forEach((key, session) -> {
            try {
            	session.getBasicRemote().sendText( userName + " : " + message);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
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
    
    @Scheduled(fixedRate = 2000) // fixedRate = 2000 表示當前方法開始執行 2000ms(2秒鐘) 後，Spring scheduling會再次呼叫該方法
    public void serverPushNotification() {
    	sessionPools.forEach((key, session) -> {
            try {
            	session.getBasicRemote().sendText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}