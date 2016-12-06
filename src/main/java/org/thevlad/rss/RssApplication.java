package org.thevlad.rss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.thevlad.rss.feed.Feed;
import org.thevlad.rss.feed.FeedItem;
import org.thevlad.rss.feed.SyndFeedWrapper;
import org.thevlad.rss.repo.FeedItemRepository;
import org.thevlad.rss.repo.SubscriptionRepository;
import org.thevlad.rss.subscr.Subscription;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

@SpringBootApplication
@ComponentScan(basePackages = "org.thevlad.rss")
@EnableMongoRepositories(basePackages = "org.thevlad.rss.repo")
@Configuration
public class RssApplication {

	private static final Logger logger = LoggerFactory.getLogger(RssApplication.class);

	@Autowired
	private RssFeedMessageSource rssFeedMessageSource;

	
	@Bean
	@InboundChannelAdapter(value = "feedChannel", poller = @Poller(maxMessagesPerPoll = "1", fixedDelay = "10000"))
	public MessageSource<List<SyndFeedWrapper>> feedAdapter() {
		return rssFeedMessageSource;
	}

	@MessageEndpoint
	public static class Endpoint {

		@Autowired
		private FeedItemRepository feedItemRepository;

		@Autowired
		private SubscriptionRepository subscriptionRepository;

		@ServiceActivator(inputChannel = "feedChannel", poller = @Poller(maxMessagesPerPoll = "1", fixedDelay = "10000"))
		public void handleFeeds(Message<List<SyndFeedWrapper>> message) throws IOException {
			List<SyndFeedWrapper> syndFeeds = message.getPayload();
			for (SyndFeedWrapper syndFeedWrapper: syndFeeds) {
				SyndFeed syndFeed = syndFeedWrapper.getSyndFeed();
				String feedUrl = syndFeed.getLink();
				if (feedUrl == null) {
					logger.warn("Feed Link undefined for FEED[title]: {}", syndFeed.getTitle());
					continue;
				}
				
				Subscription s = subscriptionRepository.findByRssUrl(syndFeedWrapper.getRssUrs()); 
				if (s == null) {
					logger.error("No subscription found for URL: " + syndFeedWrapper.getRssUrs());
					continue;
				}
				
				Feed feed = s.getFeed();
				if (feed != null && feed.getLastBuildDate().equals(syndFeed.getPublishedDate())) {
					logger.info("Found Feed {} - has actual date and will not be replaced with a new", feedUrl);
					continue;
				} else {
					feed = new Feed(feedUrl, syndFeed.getTitle(), syndFeed.getPublishedDate());
					s.setFeed(feed);
					subscriptionRepository.save(s);
				}
				
				List<FeedItem> items = new ArrayList<>();

				logger.info("Rss feed Title: {}, URL: {}\n", syndFeed.getTitle(), syndFeed.getUri());
				
				for (SyndEntry syndEntry : syndFeed.getEntries()) {
					logger.info("\trss feed item infomation. author={}, title={}, link={}", syndEntry.getAuthor(),
							syndEntry.getTitle(), syndEntry.getLink());
					
					String guid = syndEntry.getUri();
			        if (StringUtils.isBlank(guid)) {
			          guid = syndEntry.getLink();
			        }
			        if (StringUtils.isBlank(guid)) {
			          continue;
			        }
					FeedItem feedItem = new FeedItem(null, s.getId(), guid, syndEntry.getTitle(), syndEntry.getDescription().getValue(), syndEntry.getAuthor(), syndEntry.getPublishedDate());
					items.add(feedItem);
				}
				feedItemRepository.save(items);
			}
		}

	}

	@Bean(name = "feedChannel")
	public PollableChannel feedChannel() {
		return new QueueChannel();
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(RssApplication.class).web(true).run(args);
	}

}
