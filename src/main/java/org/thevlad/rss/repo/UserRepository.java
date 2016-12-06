package org.thevlad.rss.repo;

import org.springframework.data.repository.CrudRepository;
import org.thevlad.rss.sec.User;

public interface UserRepository extends CrudRepository<User, String>{
	
	User findByUserName(String userName);

}
