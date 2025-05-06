package com.websocket.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
/*
* @ServerEndpoint 註解是一個類層次的註解，它的功能主要是將目前的類定義成一個websocket伺服器端,
* 客戶端可以通過這個URL來連線到WebSocket伺服器端
*/
@ServerEndpoint(value = "/socket")
public class WebSocketServer {

    //執行緒安全的Set，用来存放每個客户端的連線。
    private static Set<Session> sessionPools = Collections.synchronizedSet(new HashSet<Session>());
    
    /**
     * 連線建立成功呼叫的方法
     * @param session 客戶端與伺服器端建立的連線
     */
    @OnOpen
    public void onOpen(Session session){
        sessionPools.add(session);
    }
    
    //@Scheduled(fixedRate = 2000) // fixedRate = 2000 表示當前方法開始執行 2000ms(2秒鐘) 後，Spring scheduling會再次呼叫該方法
    public void serverPushNotification() {
    	sessionPools.forEach(session -> {
            try {
            	session.getBasicRemote().sendText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 連線關閉呼叫的方法
     * @param Session session
     */
    @OnClose
    public void onClose(Session session){
        sessionPools.remove(session);
    }

    /**
     * 收到客户端消息時觸發（群發）這裡沒用到
     * @param session
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException{
    	sessionPools.forEach(s -> {
            try {
            	s.getBasicRemote().sendText(message);
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
}