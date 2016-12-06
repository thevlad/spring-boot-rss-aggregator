package org.thevlad.rss.feed;

import com.rometools.rome.feed.synd.SyndFeed;

public class SyndFeedWrapper {

	private String rssUrs;
	private SyndFeed syndFeed;

	public SyndFeedWrapper() {
	}

	public SyndFeedWrapper(String rssUrs, SyndFeed syndFeed) {
		this.rssUrs = rssUrs;
		this.syndFeed = syndFeed;
	}

	public String getRssUrs() {
		return rssUrs;
	}

	public void setRssUrs(String rssUrs) {
		this.rssUrs = rssUrs;
	}

	public SyndFeed getSyndFeed() {
		return syndFeed;
	}

	public void setSyndFeed(SyndFeed syndFeed) {
		this.syndFeed = syndFeed;
	}

}
