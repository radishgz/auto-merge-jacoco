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
package com.mountainminds.eclemma.internal.core.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

import com.mountainminds.eclemma.autoMerge.CoverageBuilder;
import com.mountainminds.eclemma.autoMerge.MergeFile2;
import com.mountainminds.eclemma.core.EclEmmaStatus;
import com.mountainminds.eclemma.internal.core.DebugOptions;
import com.mountainminds.eclemma.internal.core.DebugOptions.ITracer;

/**
 * Analyzes the class files that belong to given package fragment roots. This
 * analyzer implements an cache to remember the class files that have been
 * analyzed before.
 */

// TODO
// 改为只分析有执行结果的类
public class PackageFragementRootAnalyzer {

  private static final ITracer TRACER = DebugOptions.ANALYSISTRACER;

  private final ExecutionDataStore executiondata;

  public ExecutionDataStore getExecutiondata() {
    return executiondata;
  }

  public Map<Object, AnalyzedNodes> getCache() {
    return cache;
  }

  private final Map<Object, AnalyzedNodes> cache;

  public PackageFragementRootAnalyzer(final ExecutionDataStore executiondata) {
    this.executiondata = executiondata;

    this.cache = new HashMap<Object, AnalyzedNodes>();

  }

  public AnalyzedNodes analyze(final IPackageFragmentRoot root)
      throws CoreException {
    AnalyzedNodes nodes = null;
    if (!root.isExternal()) {
      nodes = analyzeInternal(root);
    }

    MergeFile2 mergeFile2 = new MergeFile2();
    try {
      mergeFile2.AutoMergeProjectSessions(this, root);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    nodes = new AnalyzedNodes(nodes.classmap.values(), nodes.sourcemap.values());
    cache.put(getClassfilesLocation(root), nodes);
    return nodes;

  }

  private AnalyzedNodes analyzeInternal(final IPackageFragmentRoot root)
      throws CoreException {
    IResource location = null;
    try {
      location = getClassfilesLocation(root);

      if (location == null) {
        TRACER.trace("No class files found for package fragment root {0}", //$NON-NLS-1$
            root.getPath());
        return AnalyzedNodes.EMPTY;
      }
      IFolder rootfolder = root.getJavaProject().getProject().getParent()
          .getFolder(root.getJavaProject().getOutputLocation());
      AnalyzedNodes nodes = cache.get(location);
      if (nodes != null) {
        return nodes;
      }

      final CoverageBuilder builder = new CoverageBuilder();
      final Analyzer analyzer = new Analyzer(executiondata, builder);
      for (ExecutionData execData : executiondata.getContents()) {
        String className = execData.getName();
        // int pos = className.lastIndexOf("/");
        // String packageName = className.substring(0, pos);

        final IFile file = (IFile) rootfolder.findMember(className + ".class");
        // String classname = packageName + "/" + file.getName();
        final InputStream in = file.getContents(true);
        try {
          analyzer.analyzeAll(in, className);
//          System.err.println("Analyse  class..." + className);

        } finally {
          in.close();
        }
      }
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
