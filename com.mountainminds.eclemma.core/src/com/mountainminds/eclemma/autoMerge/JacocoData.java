package com.mountainminds.eclemma.autoMerge;

@SuppressWarnings("nls")
public class JacocoData {

  public static String getJacocoDataDir() {
    // if (JacocoDataDir.isEmpty())
    String JacocoDataDir = LauncherInfo.getJavaproject().getProject()
        .getLocation().toFile().getAbsolutePath()
        + "/jacocoData";
    return JacocoDataDir;
  }

  // private String JacocoDataDir;

  public final static String DIRNAME = "/jacocoData";

}
