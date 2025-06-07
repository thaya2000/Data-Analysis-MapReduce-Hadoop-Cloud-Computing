package org.yarlNet.com.maxMedianAvgComputation;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MaxMedianAvgReducer extends Reducer<Text, MaxMedianAvgWritable, Text, Text> {

  private static final Logger logger = LoggerFactory.getLogger(MaxMedianAvgReducer.class);

  @Override
  protected void reduce(Text key, Iterable<MaxMedianAvgWritable> values, Context context) throws IOException, InterruptedException {
    float sum = 0f;
    long count = 0L;
    float max = 0f;
    ArrayList<Long> counts = new ArrayList<>();

    for (MaxMedianAvgWritable value : values) {
      sum += value.getAvg();
      count += value.getCount();
      if (value.getMax() > max) {
        max = value.getMax();
      }
      counts.add(value.getMedian());
    }

    Collections.sort(counts);
    long median = counts.get((counts.size() - 1) / 2);
    float avg = (count == 0) ? 0f : (sum / count);

    logger.info("Reducer MaxMedianAvgReducer emitting (key, value) pair : ({}, ({},{},{})",
        key.toString(), max, median, avg);

    context.write(key, new Text(";" + max + ";" + median + ";" + avg));
  }
}
