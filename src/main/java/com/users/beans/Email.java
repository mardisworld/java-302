package com.users.beans;

public class Email {
	private String to;
	private String subject;
	private String message;
	private String custom;
	
	protected Email() {}
	
	public Email(String to, String subject, String message, String custom) {
		this.to = to;
		this.subject = subject;
		this.message = message;
		this.custom = custom;
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

}
