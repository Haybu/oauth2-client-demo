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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sample.web.model.Order;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joe Grandja
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {
	private final Map<String, List<Order>> orders = new HashMap<>();

	@Value("${oauth2.resource.address-uri}")
	private String addressUri;

	@Autowired
	@Qualifier("addressRestTemplate")
	private OAuth2RestTemplate addressRestTemplate;

	@GetMapping("/{customerId}")
	public List<Order> getOrders(@PathVariable String customerId) {
		List<Order> orders = this.orders.get(customerId);
		if (CollectionUtils.isEmpty(orders)) {
			orders = new ArrayList<>();
		}
		return orders;
	}

	@PostMapping
	public Order placeOrder(@RequestBody Order order) {
		String validatedAddress = this.addressRestTemplate.postForObject(
				this.addressUri, order.getShipToAddress(), String.class);
		order.setShipToAddress(validatedAddress);

		order.setOrderDate(Calendar.getInstance().getTime());

		String customerIdentifier = order.getCustomerIdentifier();

		List<Order> orders = this.orders.get(customerIdentifier);
		if (CollectionUtils.isEmpty(orders)) {
			orders = new ArrayList<>();
		}
		orders.add(order);

		this.orders.put(customerIdentifier, orders);

		return order;
	}
}