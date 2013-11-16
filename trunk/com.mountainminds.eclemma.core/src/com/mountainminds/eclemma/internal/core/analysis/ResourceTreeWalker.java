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
class ResourceTreeWalker {

  private final Analyzer analyzer;

  private HashMap<String, Long> sessionClass = new HashMap<String, Long>();

  public ResourceTreeWalker(Analyzer analyzer) {
    this.analyzer = analyzer;
    Collection<ExecutionData> contents = analyzer.getExecutionData()
        .getContents();
    for (ExecutionData data : contents) {
      sessionClass.put(data.getName(), data.getId());
    }

  }

  public void walk(IResource resource) throws CoreException, IOException {
    if (resource.getType() == IResource.FILE) {
      final IFile file = (IFile) resource;
      final InputStream in = file.getContents(true);
      try {
        analyzer.analyzeAll(in, resource.getName());
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
//        System.err.println("class path:" + path + ":" + classname);
        // TODO GET PROJECT PATH
        // file.getp
        if (sessionClass.containsKey(classname)) {
          final InputStream in = file.getContents(true);
          try {
            analyzer.analyzeAll(in, resource.getName());
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
