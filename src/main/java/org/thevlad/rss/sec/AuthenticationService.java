package org.thevlad.rss.sec;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationService {

	boolean login(String username, String password, HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException;

	void logout();

	void postAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authentication);

}
