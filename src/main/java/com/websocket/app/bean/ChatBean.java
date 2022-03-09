package com.websocket.app.bean;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ChatBean {
	@Override
	public String toString() {
		return "ChatBean [to=" + to + ", type=" + type + ", users=" + users + ", msg=" + msg + "]";
	}

	private String to;
	private String type;
	private Set<String> users;
	private String msg;

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
}
