package org.yarlNet.com.computationOfAuthorshipScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AuthorshipScoreStatisticsGenerator {

  public static HashMap<String, Float> getAuthorScoreMap(Element publicationElement) {
    final float ONE_BY_FOUR = 0.25f;
    final float ONE = 1f;

    String authorTag = "author";
    String firstLabel = publicationElement.getFirstChild() != null ? publicationElement.getFirstChild().getNodeName() : "";

    if ("book".equals(firstLabel) || "proceedings".equals(firstLabel)) {
      authorTag = "editor";
    }

    NodeList authorNodes = publicationElement.getElementsByTagName(authorTag);
    int mapSize = authorNodes.getLength();
    HashMap<String, Float> authorScoreMap = new HashMap<>();
    List<String> authors = new ArrayList<>();

    if (mapSize > 0) {
      // Assign 1/N score to each author
      for (int i = 0; i < authorNodes.getLength(); i++) {
        String authorName = authorNodes.item(i).getTextContent();
        if (authorName != null && !authorName.isEmpty()) {
          authors.add(authorName);
          authorScoreMap.put(authorName, ONE / (float) mapSize);
        }
      }

      int size = mapSize - 1;

      // Execute the aforementioned algorithm iteratively
      for (int i = authors.size() - 1; i >= 0; i--) {
        String author = authors.get(i);
        if (size > 0) {
          float differential = ONE_BY_FOUR * authorScoreMap.get(author);
          authorScoreMap.put(author, authorScoreMap.get(author) - differential);
          String prevAuthor = authors.get(size - 1);
          authorScoreMap.put(prevAuthor, authorScoreMap.get(prevAuthor) + differential);
          size -= 1;
        }
      }
    }
    return authorScoreMap;
  }
}
