package com.checkout.generator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Random;

@Builder
@Data
@AllArgsConstructor
public class PageView implements KeyedRecord {
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("webpage")
    private String webPageUrl;
    @JsonProperty("postcode")
    private String postcode;
    @JsonProperty("timestamp")
    private Long timestamp;

    @Override
    public String getKey() {
        return postcode;
    }

    public static class PageViewGenerator implements RandomDatumGenerator<PageView> {
        private final int userCount;
        private final int postcodeCount;
        private final int webPageCount;
        private final Faker faker = new Faker();

        private static final String[] paths = {
            "/",
            "/about",
            "/contact",
            "/products",
            "/services",
            "/blog",
            "/news",
            "/events",
            "/careers",
            "/support"
        };


        public PageViewGenerator() {
            this.userCount = 10000;
            this.postcodeCount = 1000;
            this.webPageCount = 100;
        }

        public PageViewGenerator(int userCount, int postcodeCount, int webPageCount) {
            this.userCount = userCount;
            this.postcodeCount = postcodeCount;
            this.webPageCount = webPageCount;
        }

        @Override
        public PageView generateRandom(Random random, Instant timestamp) {
            String postcode = faker.regexify("[A-Z]{2}[0-9]{2}");
            return PageView.builder()
                    .userId(random.nextInt(userCount))
                    .webPageUrl("https://www.website.com/" + paths[random.nextInt(paths.length)])
                    .postcode(postcode)
                    .timestamp(timestamp.toEpochMilli() / 1000)
                    .build();
        }
    }
}
