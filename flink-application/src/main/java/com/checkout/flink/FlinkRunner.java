package com.checkout.flink;

import com.checkout.flink.configuration.Configuration;
import com.checkout.flink.configuration.ConfigurationOption;
import com.checkout.flink.factory.ConfigurationSelectorFactory;
import com.checkout.flink.model.AggregatePageVisitStats;
import com.checkout.flink.model.PageViewItem;
import java.time.Duration;
import java.util.Properties;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class FlinkRunner {
  private static final Configuration configuration = Configuration.getInstance();
  private static final ConfigurationOption<Integer> parallelism =
      ConfigurationOption.intOption("parallelism", "PARALLELISM", false, 1);

  private static final ConfigurationOption<String> preprocessedOutputPath =
          ConfigurationOption.stringOption("sink.file.preprocessed-path", "SINK_PREPROCESEED_FILE_PATH", true, null);
  private static final ConfigurationOption<String> aggregateOutputPath =
          ConfigurationOption.stringOption("sink.file.agg-path", "SINK_AGG_FILE_PATH", true, null);

  public static void main(String[] args) throws Exception {
    // TODO: move to a separate class and parametrize checkpointing
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(parallelism.load(configuration));
    env.enableCheckpointing(1000);
    ConfigurationSelectorFactory configurationSelectorFactory =
        ConfigurationSelectorFactory.builder().configuration(configuration).build();
    var dataStream =
        env.fromSource(
            configurationSelectorFactory.createSource(PageViewItem.class),
            WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)),
            "Kafka Source");

    Properties preprocessedProperties = new Properties();
    preprocessedProperties.put("path", preprocessedOutputPath.load(configuration));

    dataStream
            .sinkTo(configurationSelectorFactory.createSink(PageViewItem.class, preprocessedProperties))
            .name("Preprocessed File Sink");
    Properties aggProperties = new Properties();
    aggProperties.put("path", aggregateOutputPath.load(configuration));

    dataStream
        .keyBy(PageViewItem::getPostcode)
        .window(TumblingEventTimeWindows.of(Duration.ofMinutes(1)))
        .aggregate(new PageViewAggregator(), new WindowProcessFunction())
        .sinkTo(configurationSelectorFactory.createSink(AggregatePageVisitStats.class, aggProperties));
    env.execute();
  }

  public static class WindowProcessFunction
      extends ProcessWindowFunction<Integer, AggregatePageVisitStats, String, TimeWindow> {

    @Override
    public void process(
        String key,
        Context context,
        Iterable<Integer> aggregates,
        Collector<AggregatePageVisitStats> out) {
      // we know there is only one element in the iterable
      Integer aggregate = aggregates.iterator().next();
      long windowStart = context.window().getStart();
      long windowEnd = context.window().getEnd();
      out.collect(
          AggregatePageVisitStats.builder()
              .postcode(key)
              .totalVisits(aggregate)
              .startTime(windowStart)
              .endTime(windowEnd)
              .build());
    }
  }
}
