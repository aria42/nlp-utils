package edu.umass.nlp.text;


import edu.umass.nlp.utils.Span;
import net.htmlparser.jericho.*;

import java.util.List;

public class HTMLUtils {

  public static Span getSpan(Segment e) {
    return new Span(e.getBegin(), e.getEnd());
  }

  public static Span getContentSpan(Element e) {
    return getSpan(e.getContent());
  }

  public static int[] getCharOffsets(String html) {
    int[] res = new int[html.length()];
    Source source = new Source(html);
    List<Element> elems = source.getAllElements();
    for (int i=0; i < res.length; ++i) {
      res[i] = i;
    }
    for (Element elem : elems) {
      StartTag startTag = elem.getStartTag();
      Span startSpan = getSpan(startTag);
      EndTag stopTag = elem.getEndTag();
      Span stopSpan = getSpan(stopTag);
      for (int i=startSpan.getStart(); i < startSpan.getStop(); ++i) {
        res[i] -= (startSpan.getLength() - (startSpan.getStop()-i));
      }
      for (int i=startSpan.getStop(); i < stopSpan.getStart(); ++i) {
        res[i] -= startSpan.getLength();
      }
      for (int i=stopSpan.getStart(); i < stopSpan.getStop(); ++i) {
        res[i] -= (stopSpan.getLength() - (stopSpan.getStop()-i)) ;
      }
      for (int i=stopSpan.getStop(); i < html.length(); ++i) {
        res[i] -= (startSpan.getLength() + stopSpan.getLength());
      }
    }
    return res;
  }

}