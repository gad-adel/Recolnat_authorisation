package recolnat.org.authorisation.common.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class ConnectorsConfiguration {


	@Value("${collections.api.base_url}")
	private String baseUrl;

	@Value("${collections.connect.timeout}")
	private Integer timeout;

	@Bean
	@Primary
	 WebClient buildWebClient() {
		return WebClient.builder().baseUrl(baseUrl)
				.clientConnector(
						new ReactorClientHttpConnector(HttpClient
						.create()
						.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)))
				.build();
	}

}
