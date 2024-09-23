package com.checkout.flink.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties("key")
@EqualsAndHashCode
@Data
public class PageViewItem implements Serializable {
  @JsonProperty("user_id")
  private Integer userId;

  @JsonProperty("webpage")
  private String webPageUrl;

  @JsonProperty("postcode")
  private String postcode;

  @JsonProperty("timestamp")
  private Long timestamp;

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "{"
          + "user_id:"
          + userId
          + ", webpage:'"
          + webPageUrl
          + '\''
          + ", postcode:'"
          + postcode
          + '\''
          + ", timestamp:"
          + timestamp
          + '}';
    }
  }
}
