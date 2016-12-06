package org.thevlad.rss.feed;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "feedItems")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedItemShort {

	@Id
	private String id;

	private String subscriptionId;
	private String guid;
	private String title;

	public FeedItemShort() {
	}

	public FeedItemShort(String id, String subscriptionId, String guid, String title) {
		this.id = id;
		this.subscriptionId = subscriptionId;
		this.guid = guid;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
