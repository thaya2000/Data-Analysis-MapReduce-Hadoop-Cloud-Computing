package org.yarlNet.com.sortByNumCoAuthors;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class DescendingOrderByNumCoAuthorComparator extends WritableComparator {

  public DescendingOrderByNumCoAuthorComparator() {
    super(IntWritable.class, true);
  }

  @Override
  public int compare(WritableComparable a, WritableComparable b) {
    IntWritable key1 = (IntWritable) a;
    IntWritable key2 = (IntWritable) b;
    return -1 * key1.compareTo(key2); // Sorts in descending order
  }
}
