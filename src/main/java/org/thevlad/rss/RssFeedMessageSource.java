package org.thevlad.rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.thevlad.rss.feed.SyndFeedWrapper;
import org.thevlad.rss.repo.SubscriptionRepository;
import org.thevlad.rss.repo.SubscriptionService;
import org.thevlad.rss.subscr.Subscription;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Component
public class RssFeedMessageSource implements MessageSource<List<SyndFeedWrapper>> {

	private static final Logger logger = LoggerFactory.getLogger(RssFeedMessageSource.class);

	@Autowired
	private SubscriptionService subscriptionService;
	
	@Override
	public Message<List<SyndFeedWrapper>> receive() {
		return MessageBuilder.withPayload(getFeeds()).build();
	}

	private List<SyndFeedWrapper> getFeeds() {
		List<SyndFeedWrapper> feeds = new ArrayList<>();
		
		List<Subscription> subscriptions = subscriptionService.findActives();  
		if (subscriptions != null && !subscriptions.isEmpty()) {
			for (Subscription subscription : subscriptions) {
				try {
					SyndFeed feed = readFeed(subscription.getRssUrl());
					if (feed != null) {
						feeds.add(new SyndFeedWrapper( subscription.getRssUrl(), feed));
					}
				} catch (Exception e) {
					logger.error("Problem while retrieving feed: url = {}\n exception = {}", subscription.getRssUrl(), e);
				}
			}
		}
		
		return feeds;
	}

	// Arrays.asList("http://stackoverflow.com/feeds/tag?tagnames=rome",
	// "http://rss.nytimes.com/services/xml/rss/nyt/Technology.xml");

	private SyndFeed readFeed(String url) {
		
		// fetch data from URL
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		BufferedReader reader = null;
		try {
			reader = getResponseRSS(httpClient, url);
			if (reader != null) {
				feed = input.build(reader);
			}

		} catch (FeedException | IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			try {
				httpClient.close();
			} catch (IOException e) {
			}

		}
		return feed;

	}

	private  BufferedReader getResponseRSS(CloseableHttpClient httpClient, String url) throws IOException {

	    HttpGet httpGet = new HttpGet(url);

	    CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

	    BufferedReader reader;

	    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
	    } else {
	    	logger.debug("Bad Http status {}", httpResponse.getStatusLine().toString());
	        reader = null;
	    }

	    return reader;
	}

}
