package org.yarlNet.com.bucketingByNumOfNodes;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.net.URL;
import org.yarlNet.com.utils.ApplicationConstants;


public class BucketingByNumNodesMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

  org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BucketingByNumNodesMapper.class);
  private String dtdFilePath;
  private String jobName = "";
  private int bucketSize = 3;

  @Override
  protected void setup(Context context) {
    System.out.println("DEBUG: BucketingByNumNodesMapper.setup called");
    logger.info("BucketingByNumNodesMapper.setup called");

    try {
      URL dtdUrl = getClass().getClassLoader().getResource("dblp.dtd");
      if (dtdUrl != null) {
        dtdFilePath = dtdUrl.toURI().toString();
        System.out.println("DEBUG: DTD file path set to " + dtdFilePath);
      } else {
        System.out.println("DEBUG: DTD file not found!");
      }
    } catch (Exception e) {
      System.out.println("ERROR in setup: " + e.getMessage());
      throw new RuntimeException(e);
    }
    jobName = context.getConfiguration().get(ApplicationConstants.JOB_NAME);
    bucketSize = context.getConfiguration().getInt(ApplicationConstants.BUCKET_SIZE, 3);
    System.out.println("DEBUG: jobName = " + jobName + ", bucketSize = " + bucketSize);
  }

  @Override
  protected void map(LongWritable key, Text value, Context context) {
    try {
      org.w3c.dom.Element publicationData = getPublicationXMLFromPublicationText(value.toString());
      if (ApplicationConstants.BUCKETING_BY_NUM_CO_AUTHOR.equals(jobName)) {
        int authorCount = determineAuthorsPerPublication(publicationData);
        String bucket = KeyGenerator.generateKeyByBucket(authorCount, bucketSize);
        context.write(new Text(bucket), new IntWritable(1));
      } else if (ApplicationConstants.BUCKETING_BY_PUBLICATION_TYPE.equals(jobName)) {
        String publicationType = determinePublicationType(publicationData);
        context.write(new Text(publicationType), new IntWritable(1));
      }
    } catch (Exception e) {
    }
  }

  private org.w3c.dom.Element getPublicationXMLFromPublicationText(String publicationText) throws Exception {
    String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
        "<!DOCTYPE dblp SYSTEM \"" + dtdFilePath + "\"><dblp>" + publicationText + "</dblp>";
    javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
    org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
    return (org.w3c.dom.Element) doc.getDocumentElement();
  }

  private int determineAuthorsPerPublication(org.w3c.dom.Element publicationData) {
    String label = publicationData.getFirstChild().getNodeName();
    String authorTag = ("book".equals(label) || "proceedings".equals(label)) ? "editor" : "author";
    org.w3c.dom.NodeList nodes = ((org.w3c.dom.Element) publicationData.getFirstChild())
        .getElementsByTagName(authorTag);
    return nodes.getLength();
  }

  private String determinePublicationType(org.w3c.dom.Element publicationData) {
    return publicationData.getFirstChild().getNodeName();
  }
}
