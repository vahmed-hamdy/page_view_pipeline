package com.checkout.flink.factory;

import org.apache.flink.api.connector.sink2.Sink;

import java.util.Properties;

public interface SinkFactory {
  <T> Sink<T> createSink(Class<T> type, Properties args);

  String getSinkType();
}
