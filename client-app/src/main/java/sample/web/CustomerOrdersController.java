/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.OAuth2Client;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sample.web.model.Order;

import java.util.List;

/**
 * @author Joe Grandja
 */
@Controller
@RequestMapping("/orders")
public class CustomerOrdersController {

	@Value("${oauth2.resource.orders-uri}")
	private String ordersUri;

	@GetMapping
	public String getOrders(@OAuth2Client("ordering") OAuth2AuthorizedClient authorizedClient,
							OAuth2AuthenticationToken authentication,
							Model model) {

		List<Order> orders = WebClient.builder()
				.filter(oauth2Credentials(authorizedClient))
				.build()
				.get()
				.uri(this.ordersUri + "/{customerId}", authentication.getName())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Order.class)
				.collectList()
				.block();
		model.addAttribute("orders", orders);

		model.addAttribute("order", new Order());	// Used for binding to a new order

		return "orders";
	}

	@PostMapping
	public String placeOrder(@ModelAttribute Order order,
								@OAuth2Client("ordering") OAuth2AuthorizedClient authorizedClient,
								OAuth2AuthenticationToken authentication,
								Model model) {

		order.setCustomerIdentifier(authentication.getName());

		Order placedOrder = WebClient.builder()
				.filter(oauth2Credentials(authorizedClient))
				.build()
				.post()
				.uri(this.ordersUri)
				.contentType(MediaType.APPLICATION_JSON)
				.syncBody(order)
				.retrieve()
				.bodyToMono(Order.class)
				.block();

		return this.getOrders(authorizedClient, authentication, model);
	}

	private ExchangeFilterFunction oauth2Credentials(OAuth2AuthorizedClient authorizedClient) {
		return ExchangeFilterFunction.ofRequestProcessor(
				clientRequest -> {
					ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
							.build();
					return Mono.just(authorizedRequest);
				});
	}
}