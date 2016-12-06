package org.thevlad.rss;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.thevlad.rss.feed.SyndFeedWrapper;
import org.thevlad.rss.repo.SubscriptionService;
import org.thevlad.rss.subscr.Subscription;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class RssFeedMessageSourceTest {

	@Mock
	private SubscriptionService subscriptionService;
	
	@InjectMocks
	private RssFeedMessageSource rssFeedMessageSource;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReceive() {
		Subscription subscription = new Subscription(null, "http://stackoverflow.com/feeds/tag?tagnames=rome", 1);
		List<Subscription> subscriptions = new ArrayList<>();
		subscriptions.add(subscription);
		
		when(subscriptionService.findActives()).thenReturn(subscriptions);
		
		Message<List<SyndFeedWrapper>> feedsMessage = rssFeedMessageSource.receive();
		List<SyndFeedWrapper> feeds = feedsMessage.getPayload();
		assertThat(feeds, not(empty()));
	}

}
