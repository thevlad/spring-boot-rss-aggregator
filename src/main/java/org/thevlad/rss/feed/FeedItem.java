package org.thevlad.rss.feed;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "feedItems")
public class FeedItem extends FeedItemShort{

	private String descr;
	private String author;
	private Date pubDate;

	public FeedItem() {
	}

	public FeedItem(String id, String subscriptionId, String guid, String title, String descr, String author,
			Date pubDate) {
		super(id, subscriptionId, guid, title);
		this.descr = descr;
		this.author = author;
		this.pubDate = pubDate;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

}
