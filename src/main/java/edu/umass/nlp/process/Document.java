package edu.umass.nlp.process;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.BasicPair;
import edu.umass.nlp.utils.IHasProperties;
import edu.umass.nlp.utils.IPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document implements IHasProperties {
  private final Map<String,Object> properties = new HashMap();
  public final List<List<Token>> sentences = new ArrayList<List<Token>>();

  public Document(List<Token> rawTokens) {
    for (Token token : rawTokens) {
      while (token.sentIndex <= sentences.size()) {
        sentences.add(new ArrayList<Token>());
      }
      sentences.get(token.sentIndex).add(token);
    }
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public List<IPair<String, Object>> getProperties() {
    return Functional.map(properties.entrySet(), new Fn<Map.Entry<String, Object>, IPair<String, Object>>() {
      @Override
      public IPair<String, Object> apply(Map.Entry<String, Object> input) {
        return BasicPair.make(input.getKey(), input.getValue());
      }});
  }

  @Override
  public void addProperty(String name, Object val) {
    properties.put(name, val);
  }
}
