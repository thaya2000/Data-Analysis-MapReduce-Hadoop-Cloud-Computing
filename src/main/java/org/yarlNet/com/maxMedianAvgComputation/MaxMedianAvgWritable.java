package org.yarlNet.com.maxMedianAvgComputation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class MaxMedianAvgWritable implements Writable {
  private float max;
  private float avg;
  private long count;
  private long median;

  public MaxMedianAvgWritable() {
    this(0f, 0f, 0L, 0L);
  }

  public MaxMedianAvgWritable(float max, float avg, long count, long median) {
    this.max = max;
    this.avg = avg;
    this.count = count;
    this.median = median;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeFloat(max);
    out.writeFloat(avg);
    out.writeLong(count);
    out.writeLong(median);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    max = in.readFloat();
    avg = in.readFloat();
    count = in.readLong();
    median = in.readLong();
  }

  public float getMax() { return max; }
  public float getAvg() { return avg; }
  public long getCount() { return count; }
  public long getMedian() { return median; }
}
