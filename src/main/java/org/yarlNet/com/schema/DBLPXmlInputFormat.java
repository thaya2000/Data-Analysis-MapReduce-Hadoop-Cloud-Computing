package org.yarlNet.com.schema;

import com.google.common.io.Closeables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.yarlNet.com.utils.ApplicationConstants;


public class DBLPXmlInputFormat extends TextInputFormat {

  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
    try {
      return new MultiTagXmlRecordReader((FileSplit) split, context.getConfiguration());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static class MultiTagXmlRecordReader extends RecordReader<LongWritable, Text> {

    private final byte[][] possibleStartTags;
    private final byte[][] possibleEndTags;
    private final long start;
    private final long end;
    private final FSDataInputStream fsDataInputStream;
    private final DataOutputBuffer dataOutputBuffer = new DataOutputBuffer();
    private final LongWritable currentKey = new LongWritable();
    private final Text currentValue = new Text();

    public MultiTagXmlRecordReader(FileSplit split, Configuration conf) throws IOException {
      String[] startTags = conf.getStrings(ApplicationConstants.POSSIBLE_START_TAGS);
      String[] endTags = conf.getStrings(ApplicationConstants.POSSIBLE_END_TAGS);

      if (startTags == null || endTags == null) {
        throw new IOException("Start or end tags are not set in the configuration. " +
            "Please set " + ApplicationConstants.POSSIBLE_START_TAGS + " and " +
            ApplicationConstants.POSSIBLE_END_TAGS);
      }

      possibleStartTags = new byte[startTags.length][];
      for (int i = 0; i < startTags.length; i++) {
        possibleStartTags[i] = startTags[i].getBytes(StandardCharsets.UTF_8);
      }

      possibleEndTags = new byte[endTags.length][];
      for (int i = 0; i < endTags.length; i++) {
        possibleEndTags[i] = endTags[i].getBytes(StandardCharsets.UTF_8);
      }

      this.start = split.getStart();
      this.end = start + split.getLength();

      Path file = split.getPath();
      FileSystem fs = file.getFileSystem(conf);
      this.fsDataInputStream = fs.open(file);
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException {
      fsDataInputStream.seek(start);
    }

    @Override
    public boolean nextKeyValue() throws IOException {
      byte[] tag = readUntilMatch(possibleStartTags, false);

      if (fsDataInputStream.getPos() < end && tag != null) {
        try {
          dataOutputBuffer.write(tag);

          byte[] endTag = readUntilMatch(possibleEndTags, true);

          if (endTag != null) {
            currentKey.set(fsDataInputStream.getPos());
            currentValue.set(dataOutputBuffer.getData(), 0, dataOutputBuffer.getLength());
            return true;
          }
        } finally {
          dataOutputBuffer.reset();
        }
      }
      return false;
    }

    private byte[] readUntilMatch(byte[][] tags, boolean lookingForEndTag) throws IOException {
      int[] matchCounter = new int[tags.length];

      while (true) {
        int currentByte = fsDataInputStream.read();

        if (currentByte == -1) {
          return null;
        }

        if (lookingForEndTag) {
          dataOutputBuffer.write(currentByte);
        }

        for (int tagIndex = 0; tagIndex < tags.length; tagIndex++) {
          byte[] tag = tags[tagIndex];

          if (currentByte == tag[matchCounter[tagIndex]]) {
            matchCounter[tagIndex]++;
            if (matchCounter[tagIndex] >= tag.length) {
              return tag;
            }
          } else {
            matchCounter[tagIndex] = 0;
          }
        }

        if (!lookingForEndTag) {
          boolean allZero = true;
          for (int c : matchCounter) {
            if (c != 0) {
              allZero = false;
              break;
            }
          }
          if (allZero && fsDataInputStream.getPos() >= end) {
            return null;
          }
        }
      }
    }

    @Override
    public LongWritable getCurrentKey() {
      return new LongWritable(currentKey.get());
    }

    @Override
    public Text getCurrentValue() {
      return new Text(currentValue);
    }

    @Override
    public float getProgress() throws IOException {
      return (float) (fsDataInputStream.getPos() - start) / (float) (end - start);
    }

    @Override
    public void close() throws IOException {
      Closeables.close(fsDataInputStream, true);
    }
  }
}
