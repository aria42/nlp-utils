package edu.umass.nlp.exec;

import org.apache.log4j.Logger;

public class ExecutionTest {
  public static void main(String[] args) {
    Execution.init(null);
    Logger logger = Logger.getLogger("ExecutionTest");
    logger.info("Hi");
    logger.debug("Debug");
    logger.trace("ACK");
  }
}
