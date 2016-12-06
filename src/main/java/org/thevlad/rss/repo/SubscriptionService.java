package org.thevlad.rss.repo;

import java.util.List;
import java.util.Set;

import org.thevlad.rss.subscr.Subscription;

public interface SubscriptionService {

	List<Subscription> findActives();
	
	List<Subscription> findByrssUrls(Set<String> rssUrls);
	
	void incrementSubscriptionCounter(String rssUrl);
	
	void decrementSubscriptionCounter(String rssUrl);

}
