package org.yarlNet.com;

import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.log4j.PropertyConfigurator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.yarlNet.com.bucketingByNumOfNodes.BucketingByNumNodesMapper;
import org.yarlNet.com.bucketingByNumOfNodes.BucketingByNumNodesReducer;
import org.yarlNet.com.computationOfAuthorshipScore.AuthorshipScoreMapper;
import org.yarlNet.com.computationOfAuthorshipScore.AuthorshipScoreReducer;
import org.yarlNet.com.maxMedianAvgComputation.MaxMedianAvgMapper;
import org.yarlNet.com.maxMedianAvgComputation.MaxMedianAvgReducer;
import org.yarlNet.com.maxMedianAvgComputation.MaxMedianAvgWritable;
import org.yarlNet.com.schema.DBLPXmlInputFormat;
import org.yarlNet.com.sortByNumCoAuthors.DescendingOrderByNumCoAuthorComparator;
import org.yarlNet.com.sortByNumCoAuthors.InverseNumCoAuthorsMapper;
import org.yarlNet.com.sortByNumCoAuthors.InverseNumCoAuthorsReducer;
import org.yarlNet.com.sortByNumCoAuthors.SortByNumCoAuthorsMapper;
import org.yarlNet.com.sortByNumCoAuthors.SortByNumCoAuthorsReducer;
import org.yarlNet.com.utils.ApplicationConstants;


public class MapReduceJobsDriver {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MapReduceJobsDriver.class);

  public static void main(String[] args) throws Exception {
    PropertyConfigurator.configure(MapReduceJobsDriver.class.getClassLoader().getResource("log4j.properties"));

    if (args.length < 2) {
      System.err.println("No input path argument supplied, please pass an input path argument and output path argument, exiting program execution.");
      System.exit(-1);
    }

    String inputPath = args[0];
    String outputPath = args[1];

    // Load the map-reduce-job-pipeline configuration element from application.conf
    Config mapReduceJobsPipeline = ConfigFactory.load().getConfig(ApplicationConstants.MAP_REDUCE_JOB_PIPELINE);
    executeJobs(mapReduceJobsPipeline, inputPath, outputPath);
  }

  private static void executeJobs(Config mapReduceJobsPipeline, String inputPathString, String outputPathString) throws Exception {
    List<? extends Config> mapReduceJobs = mapReduceJobsPipeline.getConfigList(ApplicationConstants.JOBS);

    for (Config mapReduceJob : mapReduceJobs) {
      String jobType = mapReduceJob.getString(ApplicationConstants.JOB_TYPE);

      Configuration configuration = new Configuration();

      // Set all possible start tags into Hadoop's Configuration object
      List<String> startTags = mapReduceJobsPipeline.getStringList(ApplicationConstants.POSSIBLE_START_TAGS);
      configuration.setStrings(ApplicationConstants.POSSIBLE_START_TAGS, startTags.toArray(new String[0]));

      // Set all possible end tags into Hadoop's Configuration object
      List<String> endTags = mapReduceJobsPipeline.getStringList(ApplicationConstants.POSSIBLE_END_TAGS);
      configuration.setStrings(ApplicationConstants.POSSIBLE_END_TAGS, endTags.toArray(new String[0]));

      // Set the name of the MapReduce object
      configuration.set(ApplicationConstants.JOB_NAME, mapReduceJob.getString(ApplicationConstants.JOB_NAME));

      if (ApplicationConstants.BUCKETING.equals(jobType)) {
        if (ApplicationConstants.BUCKETING_BY_NUM_CO_AUTHOR.equals(mapReduceJob.getString(ApplicationConstants.JOB_NAME))) {
          configuration.setInt(ApplicationConstants.BUCKET_SIZE, mapReduceJob.getInt(ApplicationConstants.BUCKET_SIZE));
        }

        configuration.set(ApplicationConstants.JOB_TYPE, jobType);

        Job job = Job.getInstance(configuration);
        job.setJarByClass(MapReduceJobsDriver.class);
        job.setJobName(mapReduceJob.getString(ApplicationConstants.JOB_NAME));
        job.setMapperClass(BucketingByNumNodesMapper.class);
        job.setReducerClass(BucketingByNumNodesReducer.class);
        job.setCombinerClass(BucketingByNumNodesReducer.class);
        job.setInputFormatClass(DBLPXmlInputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set to 1 to collate reduce outputs in one file
        job.setNumReduceTasks(1);

        Path inputPath = new Path(inputPathString);

        // output path
        String outputSuffix = mapReduceJob.getString(ApplicationConstants.OUTPUT_PATH_SUFFIX);
        if (outputSuffix.startsWith("/")) {
          outputSuffix = outputSuffix.substring(1);
        }
        Path outputPath = new Path(outputPathString, outputSuffix);

        FileInputFormat.setInputPaths(job, inputPath);
        outputPath.getFileSystem(configuration).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);

        logger.info("Job Name = " + job.getJobName());
        logger.info("Execution of job " + job.getJobName() + " has begun.");
        job.waitForCompletion(true);
        logger.info("Execution of job " + job.getJobName() + " has ended.");
      }

      // MEAN_MEDIAN_MAX case
      else if (ApplicationConstants.MEAN_MEDIAN_MAX.equals(jobType)) {
        configuration.set(ApplicationConstants.JOB_TYPE, jobType);

        Job job = Job.getInstance(configuration);
        job.setJarByClass(MapReduceJobsDriver.class);
        job.setJobName(mapReduceJob.getString(ApplicationConstants.JOB_NAME));
        job.setMapperClass(MaxMedianAvgMapper.class);
        job.setReducerClass(MaxMedianAvgReducer.class);
        job.setInputFormatClass(DBLPXmlInputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MaxMedianAvgWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set to 1 to collate reduce outputs in one file
        job.setNumReduceTasks(1);

        Path inputPath = new Path(inputPathString);

        // output path
        String outputSuffix = mapReduceJob.getString(ApplicationConstants.OUTPUT_PATH_SUFFIX);
        if (outputSuffix.startsWith("/")) {
          outputSuffix = outputSuffix.substring(1);
        }
        Path outputPath = new Path(outputPathString, outputSuffix);

        FileInputFormat.setInputPaths(job, inputPath);
        outputPath.getFileSystem(configuration).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);

        logger.info("Job Name = " + job.getJobName());
        logger.info("Execution of job " + job.getJobName() + " has begun.");
        job.waitForCompletion(true);
        logger.info("Execution of job " + job.getJobName() + " has ended.");
      }

      else if (ApplicationConstants.AUTHORSHIP_SCORE.equals(jobType)) {
        configuration.set(ApplicationConstants.JOB_TYPE, jobType);

        Job job = Job.getInstance(configuration);
        job.setJarByClass(MapReduceJobsDriver.class);
        job.setJobName(mapReduceJob.getString(ApplicationConstants.JOB_NAME));
        job.setMapperClass(AuthorshipScoreMapper.class);
        job.setReducerClass(AuthorshipScoreReducer.class);
        job.setCombinerClass(AuthorshipScoreReducer.class);
        job.setInputFormatClass(DBLPXmlInputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(org.apache.hadoop.io.FloatWritable.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set to 1 to collate reduce outputs in one file
        job.setNumReduceTasks(1);

        Path inputPath = new Path(inputPathString);

        // output path
        String outputSuffix = mapReduceJob.getString(ApplicationConstants.OUTPUT_PATH_SUFFIX);
        if (outputSuffix.startsWith("/")) {
          outputSuffix = outputSuffix.substring(1);
        }
        Path outputPath = new Path(outputPathString, outputSuffix);

        FileInputFormat.setInputPaths(job, inputPath);
        outputPath.getFileSystem(configuration).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);

        logger.info("Job Name = " + job.getJobName());
        logger.info("Execution of job " + job.getJobName() + " has begun.");
        job.waitForCompletion(true);
        logger.info("Execution of job " + job.getJobName() + " has ended.");
      }

      else if (ApplicationConstants.SORT.equals(jobType)) {
        // First MapReduce job: author -> co-author count
        Job job1 = Job.getInstance(configuration);
        job1.setJarByClass(MapReduceJobsDriver.class);
        job1.setJobName(ApplicationConstants.SORT_FIRST_JOB);
        job1.setMapperClass(SortByNumCoAuthorsMapper.class);
        job1.setReducerClass(SortByNumCoAuthorsReducer.class);
        job1.setInputFormatClass(DBLPXmlInputFormat.class);
        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(Text.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        Path inputPath = new Path(inputPathString);
        String intermediateSuffix = mapReduceJob.getString(ApplicationConstants.INTERMEDIATE_SORT_OUTPUT_PATH_SUFFIX);
        if (intermediateSuffix.startsWith("/")) {
          intermediateSuffix = intermediateSuffix.substring(1);
        }
        Path outputPathFirstJob = new Path(outputPathString, intermediateSuffix);
        outputPathFirstJob.getFileSystem(configuration).delete(outputPathFirstJob, true);

        FileInputFormat.setInputPaths(job1, inputPath);
        FileOutputFormat.setOutputPath(job1, outputPathFirstJob);

        logger.info("Execution of job " + job1.getJobName() + " has begun.");
        job1.waitForCompletion(true);
        logger.info("Execution of job " + job1.getJobName() + " has ended.");

        // Second MapReduce job: sort by co-author count descending
        Job job2 = Job.getInstance(configuration);
        job2.setJarByClass(MapReduceJobsDriver.class);
        job2.setJobName(ApplicationConstants.SORT_SECOND_JOB);
        job2.setMapperClass(InverseNumCoAuthorsMapper.class);
        job2.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat.class);
        job2.setMapOutputKeyClass(IntWritable.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setSortComparatorClass(DescendingOrderByNumCoAuthorComparator.class);
        job2.setReducerClass(InverseNumCoAuthorsReducer.class);

        String completeSuffix = mapReduceJob.getString(ApplicationConstants.COMPLETE_SORT_OUTPUT_PATH_SUFFIX);
        if (completeSuffix.startsWith("/")) {
          completeSuffix = completeSuffix.substring(1);
        }
        Path outputPathSecondJob = new Path(outputPathString, completeSuffix);
        outputPathSecondJob.getFileSystem(configuration).delete(outputPathSecondJob, true);

        FileInputFormat.setInputPaths(job2, outputPathFirstJob);
        FileOutputFormat.setOutputPath(job2, outputPathSecondJob);

        // Set to 1 to collate reduce outputs into 1 file
        job2.setNumReduceTasks(1);

        logger.info("Execution of job " + job2.getJobName() + " has begun.");
        job2.waitForCompletion(true);
        logger.info("Execution of job " + job2.getJobName() + " has ended.");
      }
    }
  }
}