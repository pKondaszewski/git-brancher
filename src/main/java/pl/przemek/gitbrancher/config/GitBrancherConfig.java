package pl.przemek.gitbrancher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import pl.przemek.gitbrancher.service.github.api.GithubApiStatusHandler;

@Configuration
public class GitBrancherConfig {

    @Bean
    public RestClient restClient(@Value("${github.api.path.base}") String baseGithubApiUrl, RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .baseUrl(baseGithubApiUrl)
                .defaultStatusHandler(statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(), (request, response) ->
                        GithubApiStatusHandler.handle(response.getStatusCode().value(), response.getStatusText()))
                .build();
    }
}
