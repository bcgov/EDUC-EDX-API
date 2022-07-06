package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.APIServiceException;
import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RESTService {

    private WebClient webClient;

    @Autowired
    public RESTService(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T get(String url, Class<T> clazz) {
        T body = webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(String.class).map(NotFoundException::new)
                )
                .bodyToMono(clazz)
                .block();
        return body;
    }

}
