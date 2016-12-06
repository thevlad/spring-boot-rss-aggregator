package org.thevlad.rss.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.thevlad.rss.feed.FeedItem;
import org.thevlad.rss.feed.FeedItemShort;
import org.thevlad.rss.subscr.Subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestServicesTestIT {

	@Autowired
	private TestRestTemplate restTemplate;	

	private static String sessionToken;
	private static String firstSubscriptionId;
	private static String firstSubscriptionItemId;
	
	private static final String SESSION_TOKEN_NAME = "x-auth-token";
	@Test
	public void a_testForbidden() throws Exception {
		HttpHeaders headers = buildHeaders();

		ResponseEntity<String> exchange = restTemplate.exchange("/private/subscriptions", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

		assertThat(exchange.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}

	@Test
	public void b_testRegisterBadEmail() throws Exception {
		HttpHeaders headers = buildHeaders();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"email\":\"theUseratthemail.com\",\"password\":\"qwerty\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/public/register", HttpMethod.POST, entity, String.class);

		assertThat(exchange.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
	}
	
	@Test
	public void c_testRegisterBadPassword() throws Exception {
		HttpHeaders headers = buildHeaders();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"email\":\"theUser@themail.com\",\"password\":\"qwe\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/public/register", HttpMethod.POST, entity, String.class);

		assertThat(exchange.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
	}
	
	@Test
	public void d_testRegisterOk() throws Exception {
		HttpHeaders headers = buildHeaders();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"email\":\"theUser@themail.com\",\"password\":\"qwerty\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/public/register", HttpMethod.POST, entity, String.class);
        
        assertThat(exchange.getStatusCode(), equalTo(HttpStatus.OK));
		
		ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode responseJson = objectMapper.readTree(exchange.getBody());
	    String userName = responseJson.findValue("userName").asText();

	    assertThat( userName , equalTo("theUser@themail.com") );		
	}

	@Test
	public void e_testLoginOk() throws Exception {
		HttpHeaders headers = buildHeaders();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"email\":\"theUser@themail.com\",\"password\":\"qwerty\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/public/login", HttpMethod.POST, entity, String.class);
        
        assertThat(exchange.getStatusCode(), equalTo(HttpStatus.OK));

        List<String> tokens = exchange.getHeaders().get(SESSION_TOKEN_NAME);
        assertThat(tokens, not(empty()));
        sessionToken = tokens.get(0);
		

	    assertThat( exchange.getBody() , containsString("theUser@themail.com"));		
	}
	
	
	@Test
	public void f_testSubscribe() throws Exception {
		HttpHeaders headers = buildHeadersWithSessionToken();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"rssUrl\":\"http://stackoverflow.com/feeds/tag?tagnames=rome\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/private/subscribe", HttpMethod.POST, entity, String.class);
        assertThat(exchange.getStatusCode(), equalTo(HttpStatus.OK));
	}

	@Test
	public void g_testSubscriptions() throws Exception {
		Thread.sleep(15000);
		HttpHeaders headers = buildHeadersWithSessionToken();
		ResponseEntity<String> exchange = restTemplate.exchange("/private/subscriptions", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		String body = exchange.getBody();
		assertThat(body, not(nullValue()));
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(exchange.getBody());		
		JsonNode dataNode = rootNode.findValue("data");
		List<Subscription> subscriptions = new ArrayList<>();
		if (dataNode.isArray()) {
            for (JsonNode elementNode : dataNode) {
            	subscriptions.add(objectMapper.treeToValue(elementNode, Subscription.class));
            }
		}
		
		assertThat(subscriptions, not(empty()));
		
		firstSubscriptionId = subscriptions.get(0).getId();
		assertThat(firstSubscriptionId, not(nullValue()));

	}

	@Test
	public void h_testSubscriptionDetails() throws Exception {
		HttpHeaders headers = buildHeadersWithSessionToken();
		
		ResponseEntity<String> exchange = restTemplate.exchange("/private/subscriptions/" + firstSubscriptionId, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		String body = exchange.getBody();
		assertThat(body, not(nullValue()));

		
		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode rootNode = objectMapper.readTree(exchange.getBody());		
		JsonNode dataNode = rootNode.findValue("data");
		List<FeedItemShort> feedItems = new ArrayList<>();

		if (dataNode.isArray()) {
            for (JsonNode elementNode : dataNode) {
            	feedItems.add(objectMapper.treeToValue(elementNode, FeedItemShort.class));
            }
		}

		assertThat(feedItems, not(empty()));
		
		firstSubscriptionItemId = feedItems.get(0).getId();
		assertThat(firstSubscriptionItemId, not(nullValue()));
		
	}
	 

	@Test
	public void i_testSubscriptionDetailsItem() throws Exception {
		HttpHeaders headers = buildHeadersWithSessionToken();
		
		ResponseEntity<String> exchange = restTemplate.exchange("/private/subscriptions/detail/" + firstSubscriptionItemId, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		String body = exchange.getBody();
		assertThat(body, not(nullValue()));

		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(exchange.getBody());		
		JsonNode dataNode = rootNode.findValue("data");
		
		FeedItem feedItem = objectMapper.treeToValue(dataNode, FeedItem.class);
		assertThat(feedItem, not(nullValue()));
		
	}
	
	
	@Test
	public void j_testUnsubscribe() {
		HttpHeaders headers = buildHeadersWithSessionToken();
		
        HttpEntity<String> entity = new HttpEntity<String>("{\"rssUrl\":\"http://stackoverflow.com/feeds/tag?tagnames=rome\"}", headers);
        ResponseEntity<String> exchange = restTemplate.exchange("/private/unsubscribe", HttpMethod.POST, entity, String.class);
        assertThat(exchange.getStatusCode(), equalTo(HttpStatus.OK));
	}


	@Test
	public void k_testLogout() throws Exception {
		HttpHeaders headers = buildHeadersWithSessionToken();
		
		ResponseEntity<String> exchange = restTemplate.exchange("/private/logout", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		String body = exchange.getBody();
		assertThat(body, not(nullValue()));
		
		ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode responseJson = objectMapper.readTree(exchange.getBody());
	    boolean success = responseJson.findValue("success").asBoolean();

	    assertThat( success , is(true) );		

	}
	
	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private HttpHeaders buildHeadersWithSessionToken() {
		HttpHeaders headers = buildHeaders();
		headers.add(SESSION_TOKEN_NAME, sessionToken);
		return headers;
	}
	
}
