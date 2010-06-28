package edu.umass.nlp.exec;

import edu.umass.nlp.io.IOUtils;
import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Execution {

  private static OptionManager globalOptManager;

  public static class Opts {    
    @Opt
    public String execPoolDir = "execs/";

    @Opt
    public String execDir = null;

    @Opt
    public boolean appendDate = false;

    @Opt
    public String loggerPattern = "%-5p [%c]: %m%n";

    @Opt
    public String tag;

    private void createExecDir() {
      File rootDir = new File(execPoolDir);

      if (appendDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar c = Calendar.getInstance();
        String dateStr = sdf.format(c.getTime());
        rootDir = new File(rootDir, dateStr);
      }

      if (!rootDir.exists()) {
        rootDir.mkdirs();
      }

      File[] files = rootDir.listFiles();
      int lastNum = 0;
      for (File file : files) {
        String fname = file.getName();
        Matcher matcher = Pattern.compile("(\\d+).exec").matcher(fname);
        if (matcher.matches()) {
          int num = Integer.parseInt(matcher.group(1));
          if (num >= lastNum) lastNum = num + 1;
        }
      }
      File toCreate = new File(rootDir, "" + lastNum + ".exec");
      toCreate.mkdir();
      execDir = toCreate.getPath();
    }


  public void init() {
    if (execDir == null) {
      createExecDir();
    }
  }

}

  public static Opts opts;

  public static String getExecutionDirectory() {
    return (new File(opts.execDir)).getAbsolutePath();
  }

  public static <T> T fillOptions(String group, T o) {
    return (T) globalOptManager.fillOptions(group, o);
  }

  public static void init() {
    BasicConfigurator.configure();
  }

  public static void init(String configFile) {
    if (configFile == null) {
      throw new RuntimeException("No config file: " + configFile);      
    }
    globalOptManager = new OptionManager(configFile);
    opts = (Opts) globalOptManager.fillOptions("exec", new Opts());
    opts.init();
    initRootLogger();
    Logger logger = Logger.getLogger("Execution") ;
    logger.info("ExecutionDirectory: " + getExecutionDirectory());
    boolean created = (new File(getExecutionDirectory())).mkdirs();
    if  (created) {
      logger.info("Created " + getExecutionDirectory());
    }
    if (opts.tag != null) Logger.getLogger("Execution").info("tag: " + opts.tag);
    IOUtils.copy(configFile, getExecutionDirectory() + "/config.yaml");
  }

  private static void initRootLogger() {
    try {
      Logger.getRootLogger().addAppender(
      new FileAppender(
            new PatternLayout(opts.loggerPattern),
            (new File(getExecutionDirectory(),"out.log")).getAbsolutePath()));
      Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout(opts.loggerPattern), "System.out"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

}
