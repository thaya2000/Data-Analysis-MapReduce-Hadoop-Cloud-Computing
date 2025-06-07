package org.yarlNet.com.sortByNumCoAuthors;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SortByNumCoAuthorsMapper extends Mapper<LongWritable, Text, Text, Text> {

  private static final Logger logger = LoggerFactory.getLogger(SortByNumCoAuthorsMapper.class);
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
      Element publicationXML = getPublicationXML(value.toString());
      List<String> authors = getCoAuthors(publicationXML);

      if (!authors.isEmpty()) {
        for (String author : authors) {
          for (String coAuthor : authors) {
            if (!author.equals(coAuthor)) {
              logger.info("Mapper SortByNumCoAuthorsMapper emitting (key, value) pair : ({}, {})", author, coAuthor);
              context.write(new Text(author), new Text(coAuthor));
            }
          }
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
    org.w3c.dom.Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(xmlString.getBytes("ISO-8859-1")));
    doc.getDocumentElement().normalize();
    return doc.getDocumentElement();
  }

  private List<String> getCoAuthors(Element publicationElement) {
    List<String> authors = new ArrayList<>();
    String authorTag = "author";
    String firstLabel = publicationElement.getFirstChild() != null ? publicationElement.getFirstChild().getNodeName() : "";

    if ("book".equals(firstLabel) || "proceedings".equals(firstLabel)) {
      authorTag = "editor";
    }

    NodeList nodes = publicationElement.getElementsByTagName(authorTag);
    for (int i = 0; i < nodes.getLength(); i++) {
      String name = nodes.item(i).getTextContent();
      if (name != null && !name.isEmpty()) {
        authors.add(name);
      }
    }
    return authors;
  }
}
