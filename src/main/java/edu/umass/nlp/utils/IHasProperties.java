package edu.umass.nlp.utils;

import java.util.List;

public interface IHasProperties {

  public Object getProperty(String name);
  public List<IPair<String,Object>> getProperties();
  public void addProperty(String name, Object val);

}
