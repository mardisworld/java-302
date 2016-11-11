package com.users.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.users.security.Role;

@Entity
@Table(name = "user_roles")
public class UserRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private long userId;
	private String role;

	protected UserRole() {}
	
	protected UserRole(User user, long userId) {	
		this.userId = user.getId();
		}
	

	public UserRole(User user, Role role) {	//alternate constructor given in step5, #26
		this.userId = user.getId();
		this.role = role.toString().substring(role.toString().indexOf('_') + 1);
	}

	
	public UserRole(long userId, String role) {
		this.userId = userId;
		this.role = role;
	}

	@Override
	public String toString() {
		return "UserRole [id=" + id + ", userId=" + userId + ", role=" + role + "]";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
