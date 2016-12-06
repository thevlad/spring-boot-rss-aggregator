package org.thevlad.rss.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thevlad.rss.feed.FeedItem;
import org.thevlad.rss.feed.FeedItemShort;
import org.thevlad.rss.repo.FeedItemRepository;
import org.thevlad.rss.repo.SubscriptionRepository;
import org.thevlad.rss.repo.SubscriptionService;
import org.thevlad.rss.repo.UserRepository;
import org.thevlad.rss.sec.User;
import org.thevlad.rss.sec.UserDetailsWrapper;
import org.thevlad.rss.subscr.Subscription;

@RestController
public class FrontController {

	@Autowired
	private FeedItemRepository feedItemRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private SubscriptionService subscriptionService;
	
	@RequestMapping(path = "/private/subscriptions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> subscriptions() {

		List<Subscription> subscriptions;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsWrapper user = (UserDetailsWrapper) authentication.getPrincipal();

		if (user == null) {
			String errMsg = "No registered User was found in the current session. Please try login again.";
			return new ResponseEntity<>(ResponseMap.mapError(errMsg), HttpStatus.BAD_REQUEST);
		} else {
			Set<String> urls = user.getUser().getRssSubscriptions();
			if (CollectionUtils.isEmpty(urls)) {
				subscriptions = new ArrayList<>();
			} else {
				subscriptions = subscriptionService.findByrssUrls(urls);

			}
		}
		return new ResponseEntity<>(ResponseMap.mapOK(subscriptions), HttpStatus.OK);

	}

	@RequestMapping(path = "/private/subscriptions/{subscriptionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> subscriptionDetails(@PathVariable(value = "subscriptionId") String subscriptionId) {
		List<FeedItemShort> items = (List<FeedItemShort>) feedItemRepository.findBySubscriptionId(subscriptionId);

		return new ResponseEntity<>(ResponseMap.mapOK(items), HttpStatus.OK);
	}

	@RequestMapping(path = "/private/subscriptions/detail/{feedItemId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> subscriptionDetailsItem(@PathVariable(value = "feedItemId") String feedItemId) {
		FeedItem item = feedItemRepository.findOne(feedItemId);

		return new ResponseEntity<>(ResponseMap.mapOK(item), HttpStatus.OK);
	}
	
	@RequestMapping(path = "/public/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> register(@RequestBody(required = true) @Valid RegistrationRequest registrationRequest) {

		User user = userRepository.findByUserName(registrationRequest.getEmail().trim().toLowerCase());
		if (user != null) {
			String errMsg = "User with the same already exist. Please try another email.";
			return new ResponseEntity<>(ResponseMap.mapError(errMsg), HttpStatus.BAD_REQUEST) ;
		} else {
			Set<String> roles = new HashSet<>();
			roles.add("SUBSCRIBER");
			
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPassword = passwordEncoder.encode(registrationRequest.getPassword());
			
			User newUser = new User(registrationRequest.getEmail(), hashedPassword , roles);
			newUser = userRepository.save(newUser);
			return new ResponseEntity<>(ResponseMap.mapOK(newUser),HttpStatus.OK);

		}

	}

	@RequestMapping(path = "/private/subscribe", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> subscribe(@RequestBody(required = true) @Valid SubscribeRequest subscribeRequest) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsWrapper user = (UserDetailsWrapper) authentication.getPrincipal();

		if (user == null) {
			String errMsg = "No registered User was found in the current session. Please try login again.";
			return new ResponseEntity<>(ResponseMap.mapError(errMsg),HttpStatus.BAD_REQUEST);
		} else {
			String rssUrl = subscribeRequest.getRssUrl().trim().toLowerCase();
			if (rssUrl.endsWith("/"))
				rssUrl = rssUrl.substring(0, rssUrl.length() - 1);

			Set<String> rssSubscriptions = user.getUser().getRssSubscriptions();
			if (rssSubscriptions == null)
				rssSubscriptions = new HashSet<>();
				user.getUser().setRssSubscriptions(rssSubscriptions);
			if (rssSubscriptions.contains(rssUrl)) {
				String errMsg = "Subscription with given URL [" + rssUrl + "] already exist";
				return new ResponseEntity<>(ResponseMap.mapError(errMsg), HttpStatus.BAD_REQUEST);
			} else {
				rssSubscriptions.add(rssUrl);
				userRepository.save(user.getUser());

				Subscription subscription = subscriptionRepository.findByRssUrl(rssUrl);
				if (subscription == null) {
					subscription = new Subscription(null, rssUrl, 1);
					subscriptionRepository.save(subscription);
				} else {
					subscriptionService.incrementSubscriptionCounter(rssUrl);
				}

				return new ResponseEntity<>(ResponseMap.mapOK("Ok"),HttpStatus.OK);

			}

		}

	}

	@RequestMapping(path = "/private/unsubscribe", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody(required = true) @Valid SubscribeRequest subscribeRequest) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsWrapper user = (UserDetailsWrapper) authentication.getPrincipal();

		if (user == null) {
			String errMsg = "No registered User was found in the current session. Please try login again.";
			return new ResponseEntity<>(ResponseMap.mapError(errMsg), HttpStatus.BAD_REQUEST);
		} else {
			String rssUrl = subscribeRequest.getRssUrl().trim().toLowerCase();
			if (rssUrl.endsWith("/"))
				rssUrl = rssUrl.substring(0, rssUrl.length() - 1);

			Set<String> rssSubscriptions = user.getUser().getRssSubscriptions();
			if (rssSubscriptions == null || !rssSubscriptions.contains(rssUrl)) {
				return new ResponseEntity<>(ResponseMap.mapError("URL: [" + rssUrl + "] is not in you subcriptions!"), HttpStatus.BAD_REQUEST);
			}
			Subscription subscription = subscriptionRepository.findByRssUrl(rssUrl);
			if (subscription == null) {
				return new ResponseEntity<>(ResponseMap.mapError("URL: [" + rssUrl + "] does not exists in the  subcriptions list!"), HttpStatus.BAD_REQUEST);
			}
			subscriptionService.incrementSubscriptionCounter(rssUrl);
			rssSubscriptions.remove(rssUrl);
			User u = userRepository.save(user.getUser());
			user.setUser(u);
			return new ResponseEntity<>(ResponseMap.mapOK("Ok"), HttpStatus.OK);

		}

	}	
	
}
