package com.checkout.generator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
public class Orders implements KeyedRecord {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("order_date")
    private Long orderDate;
    @Override
    public String getKey() {
        return null;
    }
}
