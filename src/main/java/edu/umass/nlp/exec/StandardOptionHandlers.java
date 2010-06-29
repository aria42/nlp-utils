package edu.umass.nlp.exec;

import edu.umass.nlp.functional.Fn;
import org.apache.log4j.Level;

import java.io.File;

public class StandardOptionHandlers
{

  public static Fn<String, Object> fileHandler = new Fn<String, Object>() {
    public Object apply(String input) {
      return new File(input);
    }};

  public static Fn<String, Object> logLevelHandler = new Fn<String, Object>() {
    public Object apply(String input) {
      try {
        Level logLevel = Level.toLevel(input);
        return logLevel;
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      return null;
    }};

}
