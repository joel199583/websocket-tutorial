package com.websocket.app.service;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint(value = "/socket/{name}")
public class WebSocketServer {


    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static AtomicInteger online = new AtomicInteger();

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
        addOnlineCount();
        System.out.println(userName + "加入webSocket！當前人數為" + online);
        try {
            sendMessage(session, "歡迎" + userName + "加入连接！");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                sendMessage(session, userName + " : " + message);
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
    
    /**
     * 發送訊息
     * @param session 客戶端與伺服器端建立的連線
     * @param message 訊息
     * @throws IOException
     */
    public void sendMessage(Session session, String message) throws IOException{
        if(session != null){
            session.getBasicRemote().sendText(message);
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