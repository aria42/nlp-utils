package edu.umass.nlp.exec;

import org.apache.log4j.Logger;

public class ExecutionTest {

  public static class Opts {
    @Opt
    public boolean doStuff = false;
  }

  public static void main(String[] args) {
    Execution.init("/Users/aria42/Desktop/test.yaml");
    Logger logger = Logger.getLogger("ExecutionTest");
    Opts opts = Execution.fillOptions("opts", Opts.class);
    logger.info("Hi");
    logger.debug("Debug");
    logger.trace("ACK");
    logger.info(opts.doStuff);
  }
}
