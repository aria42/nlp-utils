package edu.umass.nlp.exec;

import org.apache.log4j.BasicConfigurator;

public class Execution {

  public static void init() {
    BasicConfigurator.configure();
  }

  public static void init(String[] args) {
    init();
  }
}
