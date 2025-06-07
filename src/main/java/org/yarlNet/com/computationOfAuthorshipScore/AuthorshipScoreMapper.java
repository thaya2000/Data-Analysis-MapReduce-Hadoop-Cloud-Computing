package org.yarlNet.com.computationOfAuthorshipScore;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;

public class AuthorshipScoreMapper extends Mapper<LongWritable, Text, Text, FloatWritable> {

  private static final Logger logger = LoggerFactory.getLogger(AuthorshipScoreMapper.class);
  private String dtdFilePath;

  @Override
  protected void setup(Context context) {
    try {
      URL dtdUrl = getClass().getClassLoader().getResource("dblp.dtd");
      dtdFilePath = dtdUrl.toURI().toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize DTD path", e);
    }
  }

  @Override
  protected void map(LongWritable key, Text value, Context context) {
    try {
      Element publicationXML = getXMLElementFromXMLString(value.toString());
      HashMap<String, Float> authorScoresMap = AuthorshipScoreStatisticsGenerator.getAuthorScoreMap(publicationXML);

      for (HashMap.Entry<String, Float> entry : authorScoresMap.entrySet()) {
        logger.info("Mapper AuthorshipScoreMapper emitting (key, value) pair : ({}, {})", entry.getKey(), entry.getValue());
        context.write(new Text(entry.getKey()), new FloatWritable(entry.getValue()));
      }
    } catch (Exception e) {
      logger.error("Error in map function", e);
    }
  }

  private Element getXMLElementFromXMLString(String publicationString) throws Exception {
    String xmlStringToParse = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<!DOCTYPE dblp SYSTEM \"" + dtdFilePath + "\">"
        + "<dblp>" + publicationString + "</dblp>";

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setValidating(false);
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    org.w3c.dom.Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xmlStringToParse.getBytes("ISO-8859-1")));
    doc.getDocumentElement().normalize();
    return doc.getDocumentElement();
  }
}

