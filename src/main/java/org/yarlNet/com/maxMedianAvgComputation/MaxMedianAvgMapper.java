package org.yarlNet.com.maxMedianAvgComputation;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.*;

public class MaxMedianAvgMapper extends Mapper<LongWritable, Text, Text, MaxMedianAvgWritable> {

  private static final Logger logger = LoggerFactory.getLogger(MaxMedianAvgMapper.class);
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
      Element publicationElement = getPublicationXML(value.toString());
      List<String> authors = getAuthors(publicationElement, context.getConfiguration());

      if (!authors.isEmpty()) {
        for (String author : authors) {
          logger.info("Mapper MaxMedianAvgMapper emitting (key, value) pair : ({}, ({},{},{},{}))",
              author, authors.size() - 1, authors.size() - 1, 1, authors.size() - 1);
          context.write(
              new Text(author),
              new MaxMedianAvgWritable(
                  (float) (authors.size() - 1),
                  (float) (authors.size() - 1),
                  1L,
                  (long) (authors.size() - 1)
              )
          );
        }
      }
    } catch (Exception e) {
      logger.error("Error in map function", e);
    }
  }

  private Element getPublicationXML(String publicationText) throws Exception {
    String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<!DOCTYPE dblp SYSTEM \"" + dtdFilePath + "\">"
        + "<dblp>" + publicationText + "</dblp>";

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setValidating(false);
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xmlString.getBytes("ISO-8859-1")));
    doc.getDocumentElement().normalize();
    return (Element) doc.getDocumentElement();
  }

  private List<String> getAuthors(Element publicationElement, Configuration configuration) {
    List<String> authors = new ArrayList<>();
    String authorTag = "author";
    Node firstChild = publicationElement.getFirstChild();
    if (firstChild != null && firstChild.getNodeType() == Node.ELEMENT_NODE) {
      String label = firstChild.getNodeName();
      if ("book".equals(label) || "proceedings".equals(label)) {
        authorTag = "editor";
      }
    }
    NodeList nodes = publicationElement.getElementsByTagName(authorTag);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node != null && node.getTextContent() != null) {
        authors.add(node.getTextContent());
      }
    }
    return authors;
  }
}
