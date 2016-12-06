package org.thevlad.rss.repo;

import org.springframework.data.repository.CrudRepository;
import org.thevlad.rss.subscr.Subscription;

public interface SubscriptionRepository extends CrudRepository<Subscription, String> {

	Subscription findByRssUrl(String rssUrl);
	
}
