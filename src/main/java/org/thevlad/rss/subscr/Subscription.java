package org.thevlad.rss.subscr;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.thevlad.rss.feed.Feed;

@Document(collection = "subscriptions")
public class Subscription {

	@Id
	private String id;
	@Indexed(unique = true)
	private String rssUrl;
	private int subscrCount;
	private Feed feed;

	public Subscription() {
	}

	public Subscription(String id, String rssUrl, int subscrCount) {
		this.id = id;
		this.rssUrl = rssUrl;
		this.subscrCount = subscrCount;
	}

	public Subscription(String id, String rssUrl, int subscrCount, Feed feed) {
		this(id, rssUrl, subscrCount);
		this.feed = feed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRssUrl() {
		return rssUrl;
	}

	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}

	public int getSubscrCount() {
		return subscrCount;
	}

	public void setSubscrCount(int subscrCount) {
		this.subscrCount = subscrCount;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

}
