package org.thevlad.rss.sec;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

public class UserDetailsWrapper implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private User user;
	private Collection<? extends GrantedAuthority> authorities;

	Function<String, GrantedAuthority> fromStringToGranted = new Function<String, GrantedAuthority>() {

		@Override
		public GrantedAuthority apply(String role) {
			GrantedAuthority authority = new SimpleGrantedAuthority(role);
			return authority;
		}

	};

	public UserDetailsWrapper(User user) {
		this.user = user;
		convertAuthorities(user.getRoles());
	}

	private void convertAuthorities(Set<String> roles) {
		if (CollectionUtils.isEmpty(roles)) {
			this.authorities = new HashSet<>();
		} else {
			this.authorities = user.getRoles().stream().map(fromStringToGranted)
					.collect(Collectors.<GrantedAuthority>toSet());
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return !user.isAccountExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return !user.isAccountLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !user.isCredentialsExpired();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	public User getUser () {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDetailsWrapper other = (UserDetailsWrapper) obj;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public void setUser(User u) {
		this.user = u;
		convertAuthorities(user.getRoles());
	}
	
	
}
