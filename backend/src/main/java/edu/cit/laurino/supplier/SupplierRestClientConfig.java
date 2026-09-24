package edu.cit.laurino.supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * A 3-second connect/read timeout keeps a slow LegacySupply from hanging a
 * whole request thread, satisfying Part D's "time out slow calls" rule.
 */
@Configuration
class SupplierRestClientConfig {
    @Bean
    RestClient legacySupplyRestClient(RestClient.Builder builder, LegacySupplyProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
