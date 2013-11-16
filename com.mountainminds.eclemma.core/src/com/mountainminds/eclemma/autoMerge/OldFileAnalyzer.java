/*******************************************************************************
 * Copyright (c) 2006, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 ******************************************************************************/
package com.mountainminds.eclemma.autoMerge;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.data.ExecutionData;
//import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;

import com.mountainminds.eclemma.core.EclEmmaStatus;
import com.mountainminds.eclemma.internal.core.DebugOptions;
import com.mountainminds.eclemma.internal.core.DebugOptions.ITracer;
import com.mountainminds.eclemma.internal.core.analysis.AnalyzedNodes;

/**
 * Analyzes the class files that belong to given package fragment roots. This
 * analyzer implements an cache to remember the class files that have been
 * analyzed before.
 */

// TODO
// 改为只分析有执行结果的类
public class OldFileAnalyzer {

  private static final ITracer TRACER = DebugOptions.ANALYSISTRACER;

  private ExecutionDataStore executiondata;

  public ExecutionDataStore getExecutiondata() {
    return executiondata;
  }

  public Map<Object, AnalyzedNodes> getCache() {
    return cache;
  }

  private Map<Object, AnalyzedNodes> cache;

  private HashMap<String, SourceFileInfo> oldfiles;

  HashSet<String> sessionClass = new HashSet<String>();

  public OldFileAnalyzer(final ExecutionDataStore executiondata,
      HashMap<String, SourceFileInfo> olds) {
    this.executiondata = executiondata;
    this.cache = new HashMap<Object, AnalyzedNodes>();
    oldfiles = new HashMap<String, SourceFileInfo>();
    Collection<ExecutionData> contents = executiondata.getContents();

    for (ExecutionData data : contents) {
      String temp = data.getName();

      sessionClass.add(temp);
      System.err.println("session class:" + temp);
    }

    for (SourceFileInfo oldfile : olds.values()) {
      // if (!sessionClass.containsKey(oldfile.getClassName())) {
      oldfiles.put(oldfile.getClassName(), oldfile);
      System.err.println("old class put:" + oldfile.getClassName());

      // }

    }

  }

  public AnalyzedNodes analyze(final IPackageFragmentRoot root)
      throws CoreException {
    AnalyzedNodes nodes;
    if (root.isExternal()) {
      nodes = analyzeExternal(root);

    } else {
      nodes = analyzeInternal(root);

    }
    return nodes;

  }

  private HashSet<String> analyzePackage;

  private AnalyzedNodes analyzeInternal(final IPackageFragmentRoot root)
      throws CoreException {
    IPath location = null;
    try {
      location = root.getJavaProject().getOutputLocation();

      if (location == null) {
        TRACER.trace("No class files found for package fragment root {0}", //$NON-NLS-1$
            root.getPath());
        return AnalyzedNodes.EMPTY;
      }

      AnalyzedNodes nodes = cache.get(getClassfilesLocation(root));
      if (nodes != null) {
        ConsoleMessage.showMessage("cache existed"); //$NON-NLS-1$
        return nodes;
      }

      final CoverageBuilder builder = new CoverageBuilder();
      final Analyzer analyzer = new Analyzer(executiondata, builder);
      // binroot = resource.getProjectRelativePath();
      analyzePackage = new HashSet<String>();
      for (SourceFileInfo fileInfo : oldfiles.values()) {
        String className = fileInfo.getClassName();
        int pos = className.lastIndexOf("/");
        String packageName = className.substring(0, pos);
        if (analyzePackage.contains(packageName))
          continue;
        analyzePackage.add(packageName);
        IFolder packagefolder = root.getJavaProject().getProject().getParent()
            .getFolder(location.append(packageName));

        for (IResource classresource : packagefolder.members()) {

          if ((classresource instanceof IFile) == false)
            continue;
          HashSet<String> readMainClass = new HashSet<String>();
          final IFile file = (IFile) classresource;
          if (file.getFileExtension().equals("class") == false)
            continue;
          // String classNanme = resource.getName();

          String classname = packageName + "/" + file.getName();
          classname = classname.substring(0, classname.lastIndexOf(".class"));
          // TODO GET PROJECT PATH
          // file.getp
          String noInnerPerf = classname;
          if (classname.indexOf("$") > 0)
            noInnerPerf = classname.substring(0, classname.indexOf("$"));

          if (oldfiles.containsKey(noInnerPerf + ".java")
              && !sessionClass.contains(classname)) {
            final InputStream in = file.getContents(true);
            try {
              analyzer.analyzeAll(in, classname);
              System.err.println("Analyse old class..." + classname);
              // 如果是inner class 判断 主class是否读取过，没有就读取一次
              if (!classname.equals(noInnerPerf)
                  && readMainClass.contains(noInnerPerf) == false) {
                String parentclassname = file.getName().substring(0,
                    file.getName().indexOf("$"));
                System.err.println("Analyse old class..." + packageName + "/"
                    + parentclassname);
                IFile parentfileFile = (IFile) packagefolder
                    .findMember(parentclassname + ".class");
                InputStream parentin = parentfileFile.getContents(true);
                analyzer.analyzeAll(parentin, noInnerPerf);
                readMainClass.add(noInnerPerf);
                System.err.println("Analyse old class..." + noInnerPerf);
              }
            } finally {
              in.close();
            }
          }
        }
      }
      // new OldTreeWalker(analyzer, oldfiles).walk(location);
      nodes = new AnalyzedNodes(builder.getClasses(), builder.getSourceFiles());
      cache.put(getClassfilesLocation(root), nodes);
      return nodes;
    } catch (Exception e) {
      throw new CoreException(EclEmmaStatus.BUNDLE_ANALYSIS_ERROR.getStatus(
          root.getElementName(), location, e));
    }
  }

  private AnalyzedNodes analyzeExternal(final IPackageFragmentRoot root)
      throws CoreException {
    IPath location = null;
    try {
      location = root.getPath();

      AnalyzedNodes nodes = cache.get(location);
      if (nodes != null) {
        return nodes;
      }

      final CoverageBuilder builder = new CoverageBuilder();
      final Analyzer analyzer = new Analyzer(executiondata, builder);
      new OldTreeWalker(analyzer, oldfiles).walk(location);
      nodes = new AnalyzedNodes(builder.getClasses(), builder.getSourceFiles());
      cache.put(location, nodes);
      return nodes;
    } catch (Exception e) {
      throw new CoreException(EclEmmaStatus.BUNDLE_ANALYSIS_ERROR.getStatus(
          root.getElementName(), location, e));
    }
  }

  private IResource getClassfilesLocation(IPackageFragmentRoot root)
      throws CoreException {

    // For binary roots the underlying resource directly points to class files:
    if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
      return root.getResource();
    }

    // For source roots we need to find the corresponding output folder:
    IPath path = root.getRawClasspathEntry().getOutputLocation();
    if (path == null) {
      path = root.getJavaProject().getOutputLocation();
    }
    return root.getResource().getWorkspace().getRoot().findMember(path);
  }

}
