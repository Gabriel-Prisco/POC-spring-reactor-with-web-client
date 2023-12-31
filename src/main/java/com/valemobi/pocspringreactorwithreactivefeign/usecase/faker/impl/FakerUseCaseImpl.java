package com.valemobi.pocspringreactorwithreactivefeign.usecase.faker.impl;

import com.github.javafaker.Address;
import com.github.javafaker.Avatar;
import com.github.javafaker.DragonBall;
import com.github.javafaker.Pokemon;
import com.valemobi.pocspringreactorwithreactivefeign.domain.marketplace.Product;
import com.valemobi.pocspringreactorwithreactivefeign.usecase.faker.FakerUseCase;
import com.valemobi.pocspringreactorwithreactivefeign.usecase.faker.io.CartOutput;
import com.valemobi.pocspringreactorwithreactivefeign.usecase.faker.io.ProductsOutput;
import com.valemobi.pocspringreactorwithreactivefeign.usecase.faker.io.Rapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
@SuppressWarnings("unused")
public class FakerUseCaseImpl implements FakerUseCase {

    private final WebClient webClient;

    private final RestTemplate restTemplate;

    Logger log = LoggerFactory.getLogger(FakerUseCaseImpl.class);

    public FakerUseCaseImpl(WebClient webClient, RestTemplate restTemplate) {
        this.webClient = webClient;
        this.restTemplate = restTemplate;
    }

    @Override
    public Rapper getRapperBlocking() {
        log.info("[MarketplaceUseCaseImpl - getAvatarBlocking()]: realizando request...");
        return this.restTemplate
            .getForObject("http://localhost:8081/best/rapper", Rapper.class);
    }

    @Override
    public Mono<Rapper> getRapper() {
        log.info("[MarketplaceUseCaseImpl - getAvatar()]: realizando request...");
        return this.webClient
            .get()
            .exchangeToMono(response -> {
                if (response.statusCode().equals(HttpStatus.OK)) {
                    return response.bodyToMono(Rapper.class);
                } else if (response.statusCode().is4xxClientError()) {
                    return Mono.error(ChangeSetPersister.NotFoundException::new);
                } else {
                    return response.createException()
                        .flatMap(Mono::error);
                }
            })
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(20))
                .onRetryExhaustedThrow((y, u) -> u.failure()))
            .doOnNext(i -> log.info("[MarketplaceUseCaseImpl - getAvatar()]: request finalizada, objeto recebido."));
    }

//    @Override
//    public Mono<DragonBall> getDbz() {
//        log.info("[MarketplaceUseCaseImpl - getDbz()]: realizando request...");
//        var resp = webClient.get()
//            .uri("/carts")
//            .retrieve()
//            .bodyToMono(DragonBall.class);
//
//        log.info("[MarketplaceUseCaseImpl - getDbz()]: request finalizada, objeto recebido.");
//        return resp;
//    }
//
//    @Override
//    public Mono<Address> getAddress() {
//        log.info("[MarketplaceUseCaseImpl - getAddress()]: realizando request...");
//        var resp = webClient.get()
//            .uri("/products/search?q=smartphone")
//            .retrieve()
//            .bodyToMono(Address.class);
//
//        log.info("[MarketplaceUseCaseImpl - getAddress()]: request finalizada, objeto recebido.");
//        return resp;
//    }
//
//    @Override
//    public Mono<Pokemon> getPokemon() {
//        log.info("[MarketplaceUseCaseImpl - getPokemon()]: realizando request...");
//        var resp = webClient.get()
//            .uri("/products/search?q=fragrance")
//            .retrieve()
//            .bodyToMono(Pokemon.class);
//
//        log.info("[MarketplaceUseCaseImpl - getPokemon()]: request finalizada, objeto recebido.");
//        return resp;
//    }

}

