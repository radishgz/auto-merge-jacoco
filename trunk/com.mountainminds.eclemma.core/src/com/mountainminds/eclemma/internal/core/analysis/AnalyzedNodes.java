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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;

/**
 * Internally used container for {@link IClassCoverage} and
 * {@link ISourceFileCoverage} nodes.
 */
public class AnalyzedNodes {

  public static final AnalyzedNodes EMPTY = new AnalyzedNodes(
      Collections.<IClassCoverage> emptySet(),
      Collections.<ISourceFileCoverage> emptySet());

  public Map<String, IClassCoverage> getClassmap() {
    return classmap;
  }

  public Map<String, ISourceFileCoverage> getSourcemap() {
    return sourcemap;
  }

  public final Map<String, IClassCoverage> classmap;
  public final Map<String, ISourceFileCoverage> sourcemap;

  public AnalyzedNodes(final Collection<IClassCoverage> classes,
      final Collection<ISourceFileCoverage> sourcefiles) {
    this.classmap = new HashMap<String, IClassCoverage>();
    for (final IClassCoverage c : classes) {
      classmap.put(c.getName(), c);
    }
    this.sourcemap = new HashMap<String, ISourceFileCoverage>();
    for (final ISourceFileCoverage s : sourcefiles) {
      final String key = sourceKey(s.getPackageName(), s.getName());
      sourcemap.put(key, s);
    }
  }

  public IClassCoverage getClassCoverage(final String vmname) {
    return classmap.get(vmname);
  }

  public ISourceFileCoverage getSourceFileCoverage(final String vmpackagename,
      final String filename) {
    return sourcemap.get(sourceKey(vmpackagename, filename));
  }

  public String sourceKey(final String vmpackagename, final String filename) {
    return vmpackagename + '/' + filename;
  }

}
