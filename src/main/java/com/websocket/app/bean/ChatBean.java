package com.websocket.app.bean;

import java.util.Set;

public class ChatBean {

	private String from;
	private String to;
	private String type;
	private Set<String> users;
	private String msg;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getUsers() {
		return users;
	}

	public void setUsers(Set<String> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "ChatBean [from=" + from + ", to=" + to + ", type=" + type + ", users=" + users + ", msg=" + msg + "]";
	}
}
