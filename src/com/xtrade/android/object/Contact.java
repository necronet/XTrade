package com.xtrade.android.object;

import com.google.gson.annotations.SerializedName;

public class Contact {

	public String contactId;
	@SerializedName("first_name")
	public String firstName;
	@SerializedName("last_name")
	public String lastName;
	@SerializedName("role")
	public String role;
	public String email;
	public String phone;
	public String traderId;
	
	public Contact(){
		
	}
	
	public Contact(String contactId, String name, String role, String email, String phone, String traderId) {
		this.contactId = contactId;
		this.firstName = name;
		this.role = role;
		this.email = email;
		this.phone = phone;
		this.traderId = traderId;
	}

	@Override
	public String toString() {
		return "Contact [contactId=" + contactId + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", role=" + role + ", email="
				+ email + ", phone=" + phone + ", traderId=" + traderId + "]";
	}
	
	
		
}
