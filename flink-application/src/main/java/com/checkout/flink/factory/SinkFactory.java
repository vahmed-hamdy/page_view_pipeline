package com.checkout.flink.factory;

import org.apache.flink.api.connector.sink2.Sink;

public interface SinkFactory {
  <T> Sink<T> createSink(Class<T> type);

  String getSinkType();
}
