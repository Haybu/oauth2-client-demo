/*
 * Copyright 2012-2018 the original author or authors.
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
package sample.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

/**
 * @author Joe Grandja
 */
@Configuration
@EnableOAuth2Client
public class OAuth2ClientConfig {

	@Autowired
	@Qualifier("addressClientDetails")
	private OAuth2ProtectedResourceDetails addressClientDetails;

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;

	@Bean
	public OAuth2RestTemplate addressRestTemplate() {
		return new OAuth2RestTemplate(this.addressClientDetails, this.oauth2ClientContext);
	}

	@ConfigurationProperties(prefix = "security.oauth2.client")
	@Bean
	public OAuth2ProtectedResourceDetails addressClientDetails() {
		return new ClientCredentialsResourceDetails();
	}
}