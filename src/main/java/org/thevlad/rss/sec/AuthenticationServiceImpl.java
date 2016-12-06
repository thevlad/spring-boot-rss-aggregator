package org.thevlad.rss.sec;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component("authenticationService")
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private AuthenticationManager authManager;
	

	@Autowired
	private SessionRegistry sessionRegistry;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	


	@Override
	public boolean login(String username, String password, HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			Authentication authenticate = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			logger.debug("Authenticated success: " + authenticate.getName());
			if (authenticate.isAuthenticated()) {
				postAuthentication(request, response, authenticate);
				return true;
			}
		} catch (AuthenticationException e) {	
			logger.error("Auth exception",e);
			throw e;
		}
		return false;
	}

	@Override
	public void postAuthentication(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);	
		
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
		UserDetailsWrapper userInfo = (UserDetailsWrapper) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		sessionRegistry.registerNewSession(httpSession.getId(), userInfo);

		
		
		
	}

	@Override
	public void logout() {
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		sessionRegistry.removeSessionInformation(curRequest.getSession().getId());
		curRequest.getSession().invalidate();
		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
	}


}
