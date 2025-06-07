package org.yarlNet.com.sortByNumCoAuthors;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;

public class SortByNumCoAuthorsReducer extends Reducer<Text, Text, Text, IntWritable> {

  private static final Logger logger = LoggerFactory.getLogger(SortByNumCoAuthorsReducer.class);

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    HashSet<String> set = new HashSet<>();
    for (Text value : values) {
      set.add(value.toString());
    }
    logger.info("Reducer SortByNumCoAuthorsReducer emitting (key, value) pair : ({}, {})", key.toString(), set.size());
    context.write(key, new IntWritable(set.size()));
  }
}
