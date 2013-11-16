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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.data.ExecutionData;

/**
 * Internal utility to walk through a resource tree and identify all class
 * files.
 */
@SuppressWarnings("nls")
class OldTreeWalker {

  private final Analyzer analyzer;

  private HashMap<String, Long> OldFileClass = new HashMap<String, Long>();

  public OldTreeWalker(Analyzer analyzer,
      HashMap<String, SourceFileInfo> oldfiles) {
    this.analyzer = analyzer;
    Collection<ExecutionData> contents = analyzer.getExecutionData()
        .getContents();
    HashMap<String, Long> sessionClass = new HashMap<String, Long>();
    for (ExecutionData data : contents) {
      String temp = data.getName();
      if (temp.indexOf("$") > 0) //$NON-NLS-1$
        temp = temp.substring(0, temp.indexOf("$")); //$NON-NLS-1$
      sessionClass.put(temp, data.getId());
      ConsoleMessage.showMessage("session class:" + temp);
    }
    for (SourceFileInfo oldfile : oldfiles.values()) {
      if (!sessionClass.containsKey(oldfile.getClassName())) {
        OldFileClass.put(oldfile.getClassName(), (long) 1);
        ConsoleMessage.showMessage("session class put:"
            + oldfile.getClassName());

      }
    }

  }

  public void walk(IResource resource) throws CoreException, IOException {
    if (resource.getType() == IResource.FILE) {
      final IFile file = (IFile) resource;
      final InputStream in = file.getContents(true);
      try {
        analyzer.analyzeAll(in, resource.getName());
      } catch (Exception e) {
        ConsoleMessage.showMessage("analyze old class" + resource.getName()
            + "fail");
        ConsoleMessage.log(e);
      } finally {

        in.close();
      }
    } else {
      walkResource(resource, true);
    }
  }

  private IPath binroot;

  private void walkResource(IResource resource, boolean root)
      throws CoreException, IOException {
    if (root == true)
      binroot = resource.getProjectRelativePath();
    switch (resource.getType()) {
    case IResource.FILE:
      if (resource.getName().endsWith(".class")) { //$NON-NLS-1$
        final IFile file = (IFile) resource;
        // String classNanme = resource.getName();
        IPath path = file.getProjectRelativePath();
        path = path.makeRelativeTo(binroot);
        // System.err.println("class path:" + path.makeRelativeTo(binroot));
        String classname = path.toString().substring(0,
            path.toString().indexOf(".class"));
        // TODO GET PROJECT PATH
        // file.getp

        if (classname.indexOf("$") > 0)
          classname = classname.substring(0, classname.indexOf("$"));

        if (OldFileClass.containsKey(classname + ".java")) {
          final InputStream in = file.getContents(true);
          try {
            analyzer.analyzeAll(in, resource.getName());
//            System.err.println("Analyse old class" + path + ":" + classname);

          } finally {
            in.close();
          }
        }
      }
      break;
    case IResource.FOLDER:
    case IResource.PROJECT:
      // Do not traverse into sub-folders like ".svn"
      if (root || isJavaIdentifier(resource.getName())) {
        final IContainer container = (IContainer) resource;
        for (final IResource child : container.members()) {
          walkResource(child, false);
        }
      }
      break;
    }
  }

  public void walk(IPath path) throws IOException {
    final File file = path.toFile();
    if (file.isFile()) {
      final InputStream in = open(file);
      try {
        analyzer.analyzeAll(in, path.toString());
      } finally {
        in.close();
      }
    } else {
      walkFile(file, true);
    }
  }

  private void walkFile(File file, boolean root) throws IOException {
    if (file.isFile()) {
      if (file.getName().endsWith(".class")) { //$NON-NLS-1$
        final InputStream in = open(file);
        try {
          analyzer.analyzeAll(in, file.toString());
        } finally {
          in.close();
        }
      }
    } else {
      // Do not traverse into folders like ".svn"
      if (root || isJavaIdentifier(file.getName())) {
        for (final File child : file.listFiles()) {
          walkFile(child, false);
        }
      }
    }
  }

  private InputStream open(final File file) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  private boolean isJavaIdentifier(String name) {
    for (int i = 0; i < name.length(); i++) {
      final char c = name.charAt(i);
      if (i == 0) {
        if (!Character.isJavaIdentifierStart(c)) {
          return false;
        }
      } else {
        if (!Character.isJavaIdentifierPart(c)) {
          return false;
        }
      }
    }
    return true;
  }

}
