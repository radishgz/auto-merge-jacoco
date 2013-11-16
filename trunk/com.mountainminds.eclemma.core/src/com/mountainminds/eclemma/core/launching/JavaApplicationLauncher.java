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
package com.mountainminds.eclemma.core.launching;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;

import com.mountainminds.eclemma.autoMerge.LauncherInfo;
import com.mountainminds.eclemma.core.ScopeUtils;

/**
 * Launcher for local Java applications.
 */
public class JavaApplicationLauncher extends CoverageLauncher {

  public Set<IPackageFragmentRoot> getOverallScope(
      ILaunchConfiguration configuration) throws CoreException {
    // ConsoleMessage.showMessage("start Java App launch");

    final IJavaProject project = JavaRuntime.getJavaProject(configuration);

    // add by xie for register launcherType and run project
    LauncherInfo.setLauncherType(LauncherInfo.LauncherTypeS.JavaProject);
    LauncherInfo.setJavaproject(project);

    if (project == null) {
      return Collections.emptySet();
    } else {
      Set<IPackageFragmentRoot> sets = ScopeUtils.filterJREEntries(Arrays
          .asList(project.getAllPackageFragmentRoots()));
      // for (final IPackageFragmentRoot root : sets) {
      // ConsoleMessage.showMessage(root.getElementName());
      // }
      return sets;
    }
  }

}
