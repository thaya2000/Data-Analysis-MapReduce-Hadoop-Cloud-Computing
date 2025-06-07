package org.yarlNet.com.sortByNumCoAuthors;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InverseNumCoAuthorsMapper extends Mapper<Text, Text, IntWritable, Text> {

  private static final Logger logger = LoggerFactory.getLogger(InverseNumCoAuthorsMapper.class);

  @Override
  protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
    int intVal = Integer.parseInt(value.toString());
    logger.info("Mapper InverseNumCoAuthorsMapper emitting (key, value) pair : ({}, {})", intVal, key.toString());
    context.write(new IntWritable(intVal), key);
  }
}
