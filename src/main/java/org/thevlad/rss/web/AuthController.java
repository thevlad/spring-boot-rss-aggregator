package org.thevlad.rss.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thevlad.rss.sec.AuthenticationService;
import org.thevlad.rss.sec.UserDetailsWrapper;

@RestController
public class AuthController {

	@Autowired
	private AuthenticationService authenticationService;

	@RequestMapping(value = "/public/login", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> processLogin(@RequestBody RegistrationRequest loginRequest,
			HttpServletRequest request, HttpServletResponse response) {

		boolean success = false;
		String msg = null;
		try {
			success = authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword(), request,
					response);
		} catch (AuthenticationException e) {
			msg = e.getMessage();
		}

		if (success) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsWrapper user = (UserDetailsWrapper) authentication.getPrincipal();
			return new ResponseEntity<>(ResponseMap.mapOK("You are logged in as " + user.getUsername()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseMap.mapError(msg), HttpStatus.BAD_REQUEST);
		}

	}
	
	
	@RequestMapping(value="/private/logout", method = RequestMethod.GET)
	public Map<String, ? extends Object> processLogout(HttpServletRequest request, HttpServletResponse response) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserDetailsWrapper) {
			authenticationService.logout();
			return ResponseMap.mapOK("You are logged off sucesssfully!");
		} else {
			return ResponseMap.mapError("You are not logged in!");
		}
	}
	
}
