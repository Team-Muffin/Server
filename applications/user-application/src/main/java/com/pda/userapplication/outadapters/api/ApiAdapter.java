package com.pda.userapplication.outadapters.api;

import com.pda.apiutils.GlobalResponse;
import com.pda.exceptionhandler.exceptions.BadRequestException;
import com.pda.exceptionhandler.exceptions.InternalServerException;
import com.pda.userapplication.domains.NormalUser;
import com.pda.userapplication.services.out.CreditOutputPort;
import com.pda.userapplication.services.out.GetAssetsOutputPort;
import com.pda.userapplication.services.out.dto.res.AssetInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiAdapter implements GetAssetsOutputPort, CreditOutputPort {
    private final WebClient webClient;
    @Value("${out-service.asset.url}")
    private String assetUrl;
    @Value("${out-service.product.url}")
    private String productUrl;
    @Value("${out-service.credit.url}")
    private String creditUrl;

    @Override
    public AssetInfoResponse getAssets(NormalUser normalUser) {
        Mono<GlobalResponse<AssetInfoResponse>> mono = webClient.get().uri(assetUrl+"/assets")
            .header("front-social-id", normalUser.getFrontSocialId())
            .header("back-social-id", normalUser.getBackSocialId())
            .header("user-social-contact", normalUser.getContact())
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    throw new InternalServerException("외부 API 연결 실패: " + response.statusCode());
                }

                return response.bodyToMono(new ParameterizedTypeReference<GlobalResponse<AssetInfoResponse>>() {});
            });

        return mono.block().getData();
    }

    @Override
    public AssetInfoResponse getAssetsExcludePortfolio(NormalUser normalUser) {
        AssetInfoResponse result = webClient.get().uri(uriBuilder -> uriBuilder
                .path(assetUrl+"/assets")
                .queryParam("targets", List.of("ACCOUNT", "CARD", "FUND", "LOAN"))
                .build())
            .header("front-social-id", normalUser.getFrontSocialId())
            .header("back-social-id", normalUser.getBackSocialId())
            .header("user-social-contact", normalUser.getContact())
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful())
                    throw new BadRequestException("자산 가져오기 실패");

                return response.bodyToMono(new ParameterizedTypeReference<GlobalResponse<AssetInfoResponse>>() {});
            })
            .block()
            .getData();


        return result;
    }

    @Override
    public boolean consumeCredit(Long amount, String token) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("transactionDateTime", LocalDateTime.now());

        webClient.post().uri(creditUrl+"/credit/withdraw")
            .header("Authorization", String.format("Bearer %s", token))
            .body(BodyInserters.fromValue(body))
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    log.error("Credit Server Exception: " + response.statusCode());
                    throw new BadRequestException("크레딧 차감 실패");
                }

                return response.bodyToMono(new ParameterizedTypeReference<GlobalResponse<Void>>() {});
            }).block();

        return true;
    }
}