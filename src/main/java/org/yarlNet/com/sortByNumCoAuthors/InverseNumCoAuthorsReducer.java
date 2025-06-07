package org.yarlNet.com.sortByNumCoAuthors;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InverseNumCoAuthorsReducer extends Reducer<IntWritable, Text, Text, IntWritable> {

  private static final Logger logger = LoggerFactory.getLogger(InverseNumCoAuthorsReducer.class);

  @Override
  protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    for (Text value : values) {
      logger.info("Reducer InverseNumCoAuthorsReducer emitting (key, value) pair : ({}, {})", value.toString(), key.get());
      context.write(value, key);
    }
  }
}
