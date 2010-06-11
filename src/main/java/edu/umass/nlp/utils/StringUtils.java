package edu.umass.nlp.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

  public static List<Character> getCharacters(String s) {
    List<Character> chars = new ArrayList<Character>();
    for (int i=0; i < s.length(); ++i) {
      chars.add(s.charAt(i));
    }
    return chars;
  }

  public static String toString(List<Character> chars) {
    char[] charArr = new char[chars.size()];
    for (int i=0; i < chars.size(); ++i) {
      charArr[i] = chars.get(i);
    }
    return new String(charArr);
  }
}
