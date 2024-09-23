package com.checkout.flink.factory.filesystem;

import com.checkout.flink.configuration.Configuration;
import com.checkout.flink.configuration.ConfigurationOption;
import com.checkout.flink.factory.SinkFactory;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

@AllArgsConstructor
public class FileSystemSinkFactory implements SinkFactory {

  private final ConfigurationOption<String> outputPath =
      ConfigurationOption.stringOption("path", "SINK_FILE_PATH", true, null);

  private final Configuration configuration;

  @Override
  public <T> Sink<T> createSink(Class<T> type) {
    return FileSink.forRowFormat(
            new Path(outputPath.loadWithPrefix(configuration, "sink.file.")),
            (Encoder<T>) (object, outputStream) -> outputStream.write((object.toString()+"\n").getBytes()))
        .withRollingPolicy(
            DefaultRollingPolicy.builder()
                .withRolloverInterval(Duration.ofMinutes(2))
                .withInactivityInterval(Duration.ofSeconds(30))
                .withMaxPartSize(MemorySize.ofMebiBytes(64))
                .build())
        .build();
  }

  @Override
  public String getSinkType() {
    return "file";
  }
}
