package org.yarlNet.com.computationOfAuthorshipScore;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthorshipScoreReducer extends Reducer<Text, FloatWritable, Text, FloatWritable> {

  private static final Logger logger = LoggerFactory.getLogger(AuthorshipScoreReducer.class);

  @Override
  protected void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {
    float sum = 0f;
    for (FloatWritable val : values) {
      sum += val.get();
    }
    logger.info("Reducer AuthorshipScoreReducer emitting (key, value) pair : ({}, {})", key.toString(), sum);
    context.write(key, new FloatWritable(sum));
  }
}
