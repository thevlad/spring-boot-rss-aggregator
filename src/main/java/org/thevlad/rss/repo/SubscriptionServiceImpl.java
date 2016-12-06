package org.thevlad.rss.repo;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.thevlad.rss.subscr.Subscription;

@Component
public class SubscriptionServiceImpl implements SubscriptionService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public void incrementSubscriptionCounter(String rssUrl) {
		Query query = new Query(Criteria.where("rssUrl").is(rssUrl));
		Update update = new Update().inc("subscrCount", 1);
		mongoTemplate.updateFirst(query, update, "subscriptions");

	}

	@Override
	public void decrementSubscriptionCounter(String rssUrl) {
		Query query = new Query(Criteria.where("rssUrl").is(rssUrl));
		Update update = new Update().inc("subscrCount", -1);
		mongoTemplate.updateFirst(query, update, "subscriptions");
	}

	@Override
	public List<Subscription> findActives() {

		Query query = new Query(Criteria.where("subscrCount").gt(0));
		List<Subscription> subscriptions = mongoTemplate.find(query, Subscription.class, "subscriptions");
		return subscriptions;
	}

	@Override
	public List<Subscription> findByrssUrls(Set<String> rssUrls) {
		Query query = new Query(Criteria.where("rssUrl").in(rssUrls));
		List<Subscription> feeds = mongoTemplate.find(query, Subscription.class, "subscriptions");
		return feeds;
	}

}
