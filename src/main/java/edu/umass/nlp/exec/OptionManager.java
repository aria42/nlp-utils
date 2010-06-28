package edu.umass.nlp.exec;

import edu.umass.nlp.io.IOUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OptionManager {

  private final Map<String, Map<String,String>> globalOpts;
  private final Logger logger = Logger.getLogger("OptionManager");

  public OptionManager(String confFile) {
    globalOpts = (Map) (new Yaml()).load(IOUtils.text(confFile));
  }

  public static String getOptName(Opt opt, Field f) {
   Set<String> names = new HashSet<String>();
   if (opt != null && !opt.name().equals("[unassigned]")) {
     return opt.name();
   }
   return f.getName();   
  }

  private  Object convertToType(Class type, String val) throws Exception {
    if (type.equals(int.class) || type.equals(Integer.class)) {
      return Integer.parseInt(val);
    }
    if (type.equals(float.class) || type.equals(Float.class)) {
      return Float.parseFloat(val);
    }
    if (type.equals(double.class) || type.equals(Double.class)) {
      return Double.parseDouble(val);
    }
    if (type.equals(short.class) || type.equals(Short.class)) {
      return Short.parseShort(val);
    }
    if (type.equals(boolean.class) || type.equals(Boolean.class)) {
      return !(val != null && val.equalsIgnoreCase("false"));
    }
    if (type.isEnum()) {
      Object[] objs = ((Class) type).getEnumConstants();
      for (int i = 0; i < objs.length; ++i) {
        Object enumConst = objs[i];
        if (enumConst.toString().equalsIgnoreCase(val)) {
          return enumConst;
        }
      }
    }
    if (type.equals(File.class)) {
      File f = new File(val);
      if (!f.exists()) {
        logger.warn(String.format("File %s doesn't exits\n", f.getAbsolutePath()));
      }
      return f;
    }    
    return val;
  }

  public Object fillOptions(String optGroup, Object o) {
    Map localOpts = globalOpts.get(optGroup);
    if (localOpts == null) {
      logger.warn("Couldn't find request optionGroup " + optGroup);
      return o;
    }
    Class c = (o instanceof Class) ?
      ((Class) o) : o.getClass();
    for (Field f: c.getFields()) {
      Opt opt = f.getAnnotation(Opt.class);
//      if (!f.isAccessible()) {
//        if (opt != null) {
//          logger.warn("Field " + f + " in " + c + " not accesible");
//        }
//        continue;
//      }
//      if (opt == null) continue;
      String optName = getOptName(opt, f);
      String optVal = localOpts.get(optName) == null ?
        null :
        localOpts.get(optName).toString();
      if (optVal != null) {
        try {
          f.set(o,convertToType(f.getType(), optVal));
        } catch (Exception e) {
          logger.warn("Error setting " + optName + " with value " + optVal +
            "for class " + o.getClass().getSimpleName());
        }
      }
    }
    return o;
  }

 
}
