package org.thevlad.rss.web;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

public class RegistrationRequest {

	@NotBlank(message = "Email cannot by empty")
	@Email(message = "Not valid email")
	private String email;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 5, max = 10, message = "Password length should be between 5 and 10 characters")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
