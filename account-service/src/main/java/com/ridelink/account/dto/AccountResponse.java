package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;

public record AccountResponse(String id, String name, String email, String phone, Role role, AccountStatus status) {
	public AccountResponse(String id, String name, String email, Role role, AccountStatus status) {
		this(id, name, email, null, role, status);
	}
}