package com.checkout.flink.factory;

import java.io.Serializable;
import org.apache.flink.api.connector.source.Source;

public interface SourceFactory extends Serializable {
  <T> Source<T, ?, ?> createSource(Class<T> type);

  String getSourceType();
}
