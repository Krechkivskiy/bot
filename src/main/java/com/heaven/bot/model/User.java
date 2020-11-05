package com.heaven.bot.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

	@Id
	private String id;

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "address")
	private String address;

	@OneToMany(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY)
	private List<Order> orders;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Step> steps;

	public User() {
	}

	public User(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
}
