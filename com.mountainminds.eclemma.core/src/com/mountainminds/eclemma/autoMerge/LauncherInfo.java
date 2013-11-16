package com.mountainminds.eclemma.autoMerge;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

public class LauncherInfo {

  public static String ProjectRoot;

  public static String getProjectName() {
    return ProjectName;
  }

  public static String ProjectName;

  public static String getProjectRoot() {
    return ProjectRoot;
  }

  public enum LauncherTypeS {
    JavaProject, EclipsePlugin
  };

  private static LauncherTypeS launcherType = LauncherTypeS.JavaProject;

  private static IJavaProject javaproject;

  public static LauncherTypeS getLauncherType() {
    return launcherType;
  }

  public static void setLauncherType(LauncherTypeS launcherType) {
    LauncherInfo.launcherType = launcherType;
  }

  public static IJavaProject getJavaproject() {
    return javaproject;
  }

  public static void setJavaproject(IJavaProject javaproject) {
    LauncherInfo.javaproject = javaproject;
    if (javaproject != null) {
      IProject project = javaproject.getProject();
      ProjectRoot = project.getLocation().toFile().getParent();
      ProjectName = project.getName();
    }
  }
}
