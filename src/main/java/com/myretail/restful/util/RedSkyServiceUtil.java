package com.myretail.restful.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
public class RedSkyServiceUtil {
    private WireMockServer wireMockServer;

    @PostConstruct
    public void initRedSkyServer() {
        wireMockServer = new WireMockServer(8088);
        wireMockServer.start();
        configureFor("localhost", 8088);
        stubFor(get(urlEqualTo("/v3/pdp/tcin/13860428?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("The Big Lebowski (Blu-ray) (Widescreen)")));
        stubFor(get(urlEqualTo("/v3/pdp/tcin/54456119?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("The Big Lebowski (Blu-ray) (UltraWidescreen)")));
        stubFor(get(urlEqualTo("/v3/pdp/tcin/13264003?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("The Big Lebowski (Blu-ray) (Desktop)")));
        stubFor(get(urlEqualTo("/v3/pdp/tcin/12954218?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("The Big Lebowski (Blu-ray) (SmallScreen)")));
        stubFor(get(urlPathMatching("/testGetName"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("The Big Lebowski (Blu-ray) (SmallScreen)")));
    }

    @PreDestroy
    public void shutdown() {
        wireMockServer.stop();
    }
}
