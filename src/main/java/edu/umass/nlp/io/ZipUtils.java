package edu.umass.nlp.io;


import edu.umass.nlp.functional.CallbackFn;
import edu.umass.nlp.functional.Fn;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

  public static ZipFile getZipFile(String name) {
    try {
      return new ZipFile(name);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static InputStream getEntryInputStream(ZipFile zf, String entryName) {
    try {
      return zf.getInputStream(zf.getEntry(entryName));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<String> getEntryLines(ZipFile zf, String entryName) {
    try {
      return IOUtils.lines(getEntryInputStream(zf, entryName));
    } catch (Exception e) { e.printStackTrace(); }
    return null;
  }

  public static boolean entryExists(ZipFile zipFile, String entryName) {
    return zipFile.getEntry(entryName) != null; 
  }

  public static void main(String[] args) {
    ZipFile root = ZipUtils.getZipFile(args[0]);

  }

  public static void doZipEntry(ZipOutputStream zos, String entryName, CallbackFn entryFn) {
    try {
      ZipEntry ze = new ZipEntry(entryName);
      zos.putNextEntry(ze);
      entryFn.callback();
      zos.closeEntry();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void print(ZipOutputStream zos, String text) {
    try {
      zos.write(text.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void println(ZipOutputStream zos, String text) { print(zos, text + "\n"); }
}