package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RESTService {

    private final WebClient webClient;

    @Autowired
    public RESTService(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T get(String url, Class<T> clazz) throws Exception {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(String.class).map(NotFoundException::new)
                )
                .bodyToMono(clazz)
                .block();
    }

}
