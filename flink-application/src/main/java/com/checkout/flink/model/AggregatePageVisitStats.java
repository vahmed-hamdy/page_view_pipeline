package com.checkout.flink.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AggregatePageVisitStats implements Serializable {
  @JsonProperty("postcode")
  private String postcode;

  @JsonProperty("total_visits")
  private Integer totalVisits;

  @JsonProperty("start_time")
  private Long startTime;

  @JsonProperty("end_time")
  private Long endTime;

  @Override
  public String toString() {
    return "{"
        + "postcode:'"
        + postcode
        + '\''
        + ", total_visits:"
        + totalVisits
        + ", start_time:"
        + startTime
        + ", end_time:"
        + endTime
        + '}';
  }
}
