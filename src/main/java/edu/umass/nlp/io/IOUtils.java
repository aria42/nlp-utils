package edu.umass.nlp.io;


import edu.umass.nlp.functional.Functional;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class IOUtils {

  public static List<String> lines(InputStream is) {
    return lines(new InputStreamReader(is));
  }

  public static List<String> lines(String f) { return lines(new File(f)); }

  public static List<String> lines(File f) {
    try {
      Reader r = new FileReader(f);
      return lines(r);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  public static List<String> lines(Reader r) {
    List<String> res = new ArrayList<String>();
    try {
      BufferedReader br = new BufferedReader(r);
      while (true) {
        String line = br.readLine();
        if (line == null) break;
        res.add(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return res;
  }

  public static Reader reader(File f) {
    try {
      return new FileReader(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static InputStream inputStream(String name) {
    try {
      InputStream is = new FileInputStream(name);
      if (name.endsWith(".gz")) return new GZIPInputStream(is);
      if (name.endsWith(".zip")) return new ZipInputStream(is);
      return is;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Reader readerFromResource(String resourcePath) {
    return new InputStreamReader(ClassLoader.getSystemResourceAsStream(resourcePath));
  }

  public static List<String> linesFromResource(String resourcePath) {
    return lines(readerFromResource(resourcePath));
  }

  public static boolean exists(String f) {
    return (new File(f)).exists();
  }

  public static boolean exists(File f) {
    return f.exists();
  }

  public static String changeExt(String path, String newExt) {
    if (!newExt.startsWith(".")) {
      newExt = "." + newExt;
    }
    return path.replaceAll("\\.[^.]+$",newExt);
  }

  public static String changeDir(String path, String newDir) {
    File f = new File(path);
    return (new File(newDir,f.getName())).getPath();
  }

  public static String text(InputStream is) {
    return Functional.mkString(lines(is),"","\n","");
  }

  public static String text(String path) {
    return text(new File(path));
  }

  public static String text(Reader r) {
    return Functional.mkString(lines(r),"","\n","");
  }

  public static String text(File f) {
    return Functional.mkString(lines(f),"","\n","");
  }

  public static void writeLines(File f, List<String> lines) {
    try {
      PrintWriter writer = new PrintWriter(new FileWriter(f));
      for (String line : lines) {
        writer.println(line);
      }
      writer.flush();
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeLines(String f, List<String> lines) {
    writeLines(new File(f), lines);
  }

  public static List<Object> readObjects(InputStream is) {
    List<Object> ret = new ArrayList<Object>();
    try {
      ObjectInputStream ois = new ObjectInputStream(is);
      while (true) {
        Object o = ois.readObject();
        if (o == null) break;
        ret.add(o);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  public static Object readObject(String path) {
    InputStream is = inputStream(path);
    return readObject(is);
  }

  public static Object readObject(InputStream is) {
    try {
      ObjectInputStream ois = new ObjectInputStream(is);
      return ois.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<Object> readObjects(String path) {
    InputStream is = inputStream(path);
    List<Object> ret = readObjects(is);
    try {
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  public static void writeObject(Object o, String path) {
    try {
      OutputStream os = new FileOutputStream(new File(path));
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(o);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static PrintWriter getPrintWriter(String path) {
    try {
      return new PrintWriter(new FileWriter(path));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}