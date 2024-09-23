package com.checkout.flink;

import com.checkout.flink.model.PageViewItem;
import org.apache.flink.api.common.functions.AggregateFunction;

public class PageViewAggregator implements AggregateFunction<PageViewItem, Integer, Integer> {

  @Override
  public Integer createAccumulator() {
    return 0;
  }

  @Override
  public Integer add(PageViewItem value, Integer accumulator) {
    return ++accumulator;
  }

  @Override
  public Integer getResult(Integer accumulator) {
    return accumulator;
  }

  @Override
  public Integer merge(Integer a, Integer b) {
    return a + b;
  }
}
