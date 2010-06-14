//package edu.umass.nlp.exec;
//
//import org.apache.log4j.Logger;
//
//import java.util.*;
//
//
//public class OptionParser {
//
//  private static Map<String, String> globalOpts = new HashMap<String, String>();
//  private static Map<Class, Map<String, OptInfo>> classOptInfoMap = new HashMap<Class, Map<String, OptInfo>>();
//
//  private static void printHelpAndDie() {
//    Logger logger = Logger.getRootLogger();
//    logger.info("Options Help\n============");
//    List<Class> classes = new ArrayList<Class>(classOptInfoMap.keySet());
//    Collections.sort(classes, new Comparator<Class>() {
//      public int compare(Class a, Class b) {
//        return classOptInfoMap.get(b).size() - classOptInfoMap.get(a).size();
//      }});
//    for (Class c : classes) {
//      Logger.i().startTrack(String.format("Options for class %s", c.getSimpleName()));
//      Map<String, OptInfo> opts = classOptInfoMap.get(c);
//      if (opts.isEmpty()) break;
//      for (OptInfo optInfo : opts.values()) {
//        Logger.i().logs(optInfo.toString());
//      }
//      Logger.i().endTrack();
//    }
//    Logger.i().endTrack();
//    System.exit(0);
//  }
//
//  private static void expand(Class c) {
//    if (!registerClass(c)) return;
//    try {
////      // Interface
////      if (c.isInterface()) {
////        List<Class> implementingClasses = ReflectionUtils.getClassessOfInterface(c.getPackage().getName(), c);
////        for (Class implementingClass : implementingClasses) {
////          try {
////            expand(implementingClass);
////          } catch (Exception e) {
////          }
////        }
////      }
////      // List All Constructors
////      Constructor[] ctors = c.getConstructors();
////      for (Constructor ctor : ctors) {
////        Class[] parms = ctor.getParameterTypes();
////        for (Class parm : parms) {
////          try {
////            expand(parm);
////          } catch (Exception e) {
////          }
////        }
////      }
//      // List All Methods
//      Method[] meths = c.getMethods();
//      for (Method meth : meths) {
//        Class retClass = meth.getReturnType();
//        try {
//          expand(retClass);
//        } catch (Exception e) {
//        }
//        Class[] params = meth.getParameterTypes();
//        for (Class param : params) {
//          try {
//            expand(param);
//          } catch (Exception e) {
//          }
//        }
//      }
////      // List All Inner Classes
////      Class[] innerClasses = c.getClasses();
////      for (Class innerClass : innerClasses) {
////        try {
////          expand(innerClass);
////        } catch (Exception e) {
////        }
////      }
//      // List All Fields
//      Field[] fields = c.getFields();
//      for (Field field : fields) {
//        try {
//          expand(field.getClass());
//        } catch (Exception e) {
//        }
//      }
//    } catch (Exception e) {
//
//    }
//  }
//
//  public static List<File> getConfFiles(String[] args) {
//    List<File> confFiles = new ArrayList<File>();
//    for (String arg : args) {
//      if (arg.startsWith("++")) {
//        String file = arg.substring(2);
//        confFiles.add(new File(file));
//      }
//    }
//    return confFiles;
//  }
//
//  public static void registerArgs(String[] args, Object... rootObjs) {
//    boolean printHelp = false;
//    String cachedKey = null;
//    for (String cur : args) {
//      if (cur.equals("-h")) {
//        printHelp = true;
//      }
//      if (cur.startsWith("++")) {
//        String file = cur.substring(2);
//        registerOptionFile(file);
//        continue;
//      }
//      if (cur.startsWith("-")) {
//        cur = cur.substring(1).toLowerCase();
//        if (cachedKey != null) {
//          globalOpts.put(cachedKey, null);
//        }
//        cachedKey = cur;
//        continue;
//      }
//      if (cachedKey != null) {
//        globalOpts.put(cachedKey, cur);
//        cachedKey = null;
//      }
//    }
//    for (Object rootObj : rootObjs) {
//      Class c = rootObj instanceof Class ? (Class) rootObj : rootObj.getClass();
//      expand(c);
//    }
//
//    if (printHelp) printHelpAndDie();
//    ensureRequiredOptionsFilled();
//    ensureOptMap();
//  }
//
//  public static void logOptions() {
//    Logger.startTrack("[GlobalOptionsParser] Registered Arguments");
//    for (Map.Entry<String, String> entry : globalOpts.entrySet()) {
//      Logger.i().logs(entry.getKey() + " => " + entry.getValue());
//    }
//    Logger.endTrack();
//  }
//
//  private synchronized static void ensureOptMap() {
//    String execDir = Execution.getVirtualExecDir() != null ?
//      Execution.getVirtualExecDir() :
//      globalOpts.get("optMap");
//    if (execDir != null) {
//      File outFile = new File(new File(execDir), "opt.compose");
//      List<String> lines = new ArrayList<String>();
//      for (Map.Entry<String, String> entry : globalOpts.entrySet()) {
//        lines.add(String.format("%s %s", entry.getKey(), entry.getValue()));
//      }
//      IOUtils.writeLinesHard(outFile.getAbsolutePath(), lines);
//    }
//  }
//
//  private static void ensureRequiredOptionsFilled() {
//    List<OptInfo> missingReqOpts = new ArrayList<OptInfo>();
//    for (Map.Entry<Class, Map<String, OptInfo>> classMapEntry : classOptInfoMap.entrySet()) {
//      //Class c = classMapEntry.getKey();
//      Map<String, OptInfo> innerMap = classMapEntry.getValue();
//      for (Map.Entry<String, OptInfo> stringOptInfoEntry : innerMap.entrySet()) {
//        String optName = stringOptInfoEntry.getKey();
//        OptInfo optInfo = stringOptInfoEntry.getValue();
//        if (optInfo.opt.required() && !globalOpts.containsKey(optName)) {
//          missingReqOpts.add(optInfo);
//        }
//      }
//    }
//    if (!missingReqOpts.isEmpty()) {
//      Logger.i().startTrack("Missing required opts");
//      for (OptInfo missingReqOpt : missingReqOpts) {
//        Logger.i().logs(missingReqOpt.toString() + " in class " + missingReqOpt.targetClass.getSimpleName());
//      }
//      Logger.i().endTrack();
//      System.exit(0);
//    }
//  }
//
//  private static String getOptionName(Opt opt, OptInfo optInfo) {
//    if (!opt.name().equals("[unassigned]")) {
//      return opt.name();
//    }
//    if (optInfo.method != null) {
//      String methName = optInfo.method.getName().toLowerCase();
//      methName = methName.replaceAll("_\\$eq", "");
//      if (methName.startsWith("set")) {
//        return methName.substring(3).toLowerCase();
//      }
//      return methName;
//    }
//    if (optInfo.field != null) {
//      return optInfo.field.getName().toLowerCase();
//    }
//    throw new RuntimeException("");
//  }
//
//  private static Method findScalaSetter(Class c, Method scalaGetter) {
//    String name = scalaGetter.getName();
//    String setterName = name + "_$eq";
//    for (Method m : c.getMethods()) {
//      if (m.getName().equals(setterName) && m.getParameterTypes().length == 1) {
//        return m;
//      }
//    }
//    return null;
//  }
//
//  public static boolean registerClass(Class c) {
//    synchronized (classOptInfoMap) {
//      if (classOptInfoMap.containsKey(c)) return false;
//      List<OptInfo> optInfos = new ArrayList<OptInfo>();
//      Method[] methods = c.getMethods();
//      for (Method method : methods) {
//        Opt opt = method.getAnnotation(Opt.class);
//        if (opt != null) {
//          if (method.getParameterTypes().length == 0) {
//            continue;
//          }
//          OptInfo optInfo = new OptInfo();
//          optInfo.targetClass = c;
//          optInfo.method = method;
//          optInfo.optName = getOptionName(opt, optInfo);
//          optInfo.type = method.getParameterTypes()[0];
//          optInfo.opt = opt;
//          optInfos.add(optInfo);
//        }
//      }
//      for (Field field : c.getFields()) {
//        Opt opt = field.getAnnotation(Opt.class);
//        if (opt != null) {
//          OptInfo optInfo = new OptInfo();
//          optInfo.targetClass = c;
//          optInfo.field = field;
//          optInfo.optName = getOptionName(opt, optInfo);
//          optInfo.type = field.getType();
//          optInfo.opt = opt;
//          optInfos.add(optInfo);
//        }
//      }
//      Map<String, OptInfo> optNameToOptInfo = new HashMap<String, OptInfo>();
//      for (OptInfo optInfo : optInfos) {
//        if (optNameToOptInfo.containsKey(optInfo.optName)) {
//          System.err.printf("Error two annotations with name %s in class %s\n", optInfo.optName, optInfo.targetClass);
//          continue;
//        }
//        optNameToOptInfo.put(optInfo.optName, optInfo);
//      }
//      classOptInfoMap.put(c, optNameToOptInfo);
//    }
//    return true;
//  }
//
//  public static void registerOptionFile(String file) {
//    try {
//      BufferedReader br = new BufferedReader(new FileReader(file));
//      registerOptionFile(br);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public static void registerOptionFile(BufferedReader br) {
//    try {
//      while (true) {
//        String line = br.readLine();
//        if (line == null) break;
//        if (line.startsWith("#")) continue;
//        String[] fields = line.split("\\s+");
//        if (fields.length == 0) continue;
//        ;
//        String optName = fields[0].toLowerCase();
//        if (optName.startsWith("-")) optName = optName.substring(1);
//        if (fields.length > 1) {
//          String value = fields[1];
//          if (globalOpts.containsKey(optName)) {
//            System.err.printf("[GlobalOptionParser] Option name %s already set,ignoring %s value\n", optName, value);
//          }
//          globalOpts.put(optName, value);
//        }
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  private static class OptInfo {
//    Class targetClass; // which class
//    Method method;   // method to invoke
//    Field field;     // field to set
//    Class type;      // type we're setting
//    String optName;  // name of option
//    Opt opt;
//
//    public String toString() {
//      String s = optName;
//      s = s + ": " + type.getSimpleName();
//      if (opt.gloss().length() > 0) s = s + " " + opt.gloss();
//      if (opt.required()) s = s + " [required]";
//      return s;
//    }
//  }
//
//  private static Object getVal(OptInfo optInfo, String val) {
//    try {
//      return convertToType(optInfo.type, val);
//    } catch (Exception e) {
//      Logger.i().err("Error processing " + optInfo.optName + " with val " + val);
//    }
//    return null;
//  }
//
//  private static void fillOptions(Map<OptInfo, String> optVals, Object o) {
//    // Set Fields First
//    for (Map.Entry<OptInfo, String> entry : optVals.entrySet()) {
//      OptInfo optInfo = entry.getKey();
//      String val = entry.getValue();
//      if (optInfo.field != null) {
//        Object objVal = getVal(optInfo, val);
//        try {
//          optInfo.field.set(o, objVal);
//        } catch (Exception e) {
//          Logger.i().err("Error setting field " + optInfo.field);
//        }
//      }
//    }
//    // Set Methods Second
//    for (Map.Entry<OptInfo, String> entry : optVals.entrySet()) {
//      OptInfo optInfo = entry.getKey();
//      String val = entry.getValue();
//      if (optInfo.method != null) {
//        Object objVal = getVal(optInfo, val);
//        try {
//          optInfo.method.invoke(o, objVal);
//        } catch (Exception e) {
//          Logger.err("Error setting Option");
//          e.printStackTrace();
//        }
//      }
//    }
//  }
//
//  private static Object convertToType(Class type, String val) throws Exception {
//    if (type.equals(int.class) || type.equals(Integer.class)) {
//      return Integer.parseInt(val);
//    }
//    if (type.equals(float.class) || type.equals(Float.class)) {
//      return Float.parseFloat(val);
//    }
//    if (type.equals(double.class) || type.equals(Double.class)) {
//      return Double.parseDouble(val);
//    }
//    if (type.equals(short.class) || type.equals(Short.class)) {
//      return Short.parseShort(val);
//    }
//    if (type.equals(boolean.class) || type.equals(Boolean.class)) {
//      return !(val != null && val.equalsIgnoreCase("false"));
//    }
//    if (type.isEnum()) {
//      Object[] objs = ((Class) type).getEnumConstants();
//      for (int i = 0; i < objs.length; ++i) {
//        Object enumConst = objs[i];
//        if (enumConst.toString().equalsIgnoreCase(val)) {
//          return enumConst;
//        }
//      }
//    }
//    if (type.equals(File.class)) {
//      File f = new File(val);
//      if (!f.exists()) {
//        System.err.printf("File %s doesn't exits\n", f.getAbsolutePath());
//      }
//      return f;
//    }
//    if (type.equals(BufferedReader.class)) {
//      return new BufferedReader(new FileReader(val));
//    }
//    return val;
//  }
//
//
//  public static void fillOptions(Object o) {
//    Class c = o.getClass();
//    registerClass(c);
//    try {
//      Map<String, OptInfo> optName2Info = classOptInfoMap.get(c);
//      Map<OptInfo, String> optInfo2Val = new HashMap<OptInfo, String>();
//      for (Map.Entry<String, OptInfo> entry : optName2Info.entrySet()) {
//        String optName = entry.getKey();
//        OptInfo optInfo = entry.getValue();
//        if (globalOpts.containsKey(optName)) {
//          String optVal = globalOpts.get(optName);
//          optInfo2Val.put(optInfo, optVal);
//        } else {
//          String optVal = optInfo.opt.defaultVal();
//          if (optVal.length() > 0) optInfo2Val.put(optInfo, optVal);
//        }
//      }
//      for (OptInfo optInfo : optName2Info.values()) {
//        if (optInfo.opt.required() && !optInfo2Val.containsKey(optInfo)) {
//          System.err.printf("[GlobalOptionParser] Missing required option %s in class %s\n",
//            optInfo.optName, optInfo.targetClass.getSimpleName());
//          System.exit(2);
//        }
//      }
//      fillOptions(optInfo2Val, o);
//    } catch (Exception e) {
//      e.printStackTrace();
//      System.exit(0);
//    }
//  }
//
//  // Main Test
//
//  public static void main(String[] args) {
//    class Opts implements Runnable {
//      Opts() {
//        GlobalOptionParser.fillOptions(this);
//      }
//
//      @Opt(required = true)
//      public double x;
//
//      public void run() {
//
//      }
//    }
//    Execution.ignoreUnknownOpts = true;
//    Execution.init(args);
//    Logger.setFig();
//    GlobalOptionParser.registerArgs(args, Opts.class);
//    Opts opts = new Opts();
//    opts.run();
//    Execution.finish();
//  }
//}
