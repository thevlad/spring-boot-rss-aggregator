package org.thevlad.rss.feed;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;

public class Feed {


	@Indexed(unique = true)
	private String feedUrl;
	private String title;
	private Date lastBuildDate;

	public Feed() {
	}

	public Feed(String feedUrl, String title, Date lastBuildDate) {
		this.feedUrl = feedUrl;
		this.title = title;
		this.lastBuildDate = lastBuildDate;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getLastBuildDate() {
		return lastBuildDate;
	}

	public void setLastBuildDate(Date lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
	}

}
