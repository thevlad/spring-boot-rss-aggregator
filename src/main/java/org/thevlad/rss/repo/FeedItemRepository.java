package org.thevlad.rss.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.thevlad.rss.feed.FeedItem;
import org.thevlad.rss.feed.FeedItemShort;

public interface FeedItemRepository extends CrudRepository<FeedItem, String>{

	List<FeedItemShort> findBySubscriptionId(String subscriptionId);
	
	
}
