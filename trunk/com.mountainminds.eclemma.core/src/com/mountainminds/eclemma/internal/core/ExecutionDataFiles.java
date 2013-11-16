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
package com.mountainminds.eclemma.internal.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jacoco.core.data.ExecutionDataWriter;

import com.mountainminds.eclemma.autoMerge.ConsoleMessage;
import com.mountainminds.eclemma.core.EclEmmaStatus;
import com.mountainminds.eclemma.core.IExecutionDataSource;
import com.mountainminds.eclemma.core.URLExecutionDataSource;

/**
 * Internal utility to create and cleanup execution data files manage files in
 * the plugin's state location.
 */
public final class ExecutionDataFiles {

  private static final String FOLDER = ".execdata/"; //$NON-NLS-1$

  private final File folder;

  public ExecutionDataFiles(IPath stateLocation) {

    folder = stateLocation.append(FOLDER).toFile();
    ConsoleMessage.showMessage(folder.getAbsolutePath());
    folder.mkdirs();
  }

  public ExecutionDataFiles() {
    // com.mountainminds.eclemma.core

    // getStateLocation();
    folder = EclEmmaCorePlugin.getInstance().getStateLocation().append(FOLDER)
        .toFile();
    ConsoleMessage.showMessage(folder.getAbsolutePath());
    folder.mkdirs();
  }

  public File getFolder() {
    return folder;
  }

  /**
   * Delete any existing execution data file.
   */
  public void deleteTemporaryFiles() {
    final File[] files = folder.listFiles();
    for (final File file : files) {
      file.delete();
    }
  }

  /**
   * Creates a new execution data file containing the content of the given
   * source.
   * 
   * @param source
   *          source to dump into the file
   * @return created file
   */
  public IExecutionDataSource newFile(IExecutionDataSource source)
      throws CoreException {
    try {
      // save a copy first
//      TestnewFile(source);
      final File file = File.createTempFile("session", ".exec", folder); //$NON-NLS-1$ //$NON-NLS-2$
      final OutputStream out = new BufferedOutputStream(new FileOutputStream(
          file));

      // MemoryExecutionDataSource datasource = (MemoryExecutionDataSource)
      // source;
      // 自动合并
      // MergeFile mergeFile = new MergeFile();

      // source = mergeFile.AutoMergeProjectSessions(datasource, "description",
      // null);
      // 读取内容并写入文件
      // ConsoleMessage.showMessage("source class" +
      // source.getClass().toString());
      final ExecutionDataWriter writer = new ExecutionDataWriter(out);
      source.accept(writer, writer);
      out.close();
      ConsoleMessage.showMessage(file.getAbsolutePath());
      return new URLExecutionDataSource(file.toURL());
    } catch (IOException e) {
      throw new CoreException(EclEmmaStatus.EXEC_FILE_CREATE_ERROR.getStatus(e));
    }
  }

//  public IExecutionDataSource TestnewFile(IExecutionDataSource source)
//      throws CoreException {
//    try {
//      final File file = File.createTempFile("testsession", ".exec", folder); //$NON-NLS-1$ //$NON-NLS-2$
//      final OutputStream out = new BufferedOutputStream(new FileOutputStream(
//          file));
//
//      final ExecutionDataWriter writer = new ExecutionDataWriter(out);
//      source.accept(writer, writer);
//      out.close();
//      ConsoleMessage.showMessage(file.getAbsolutePath());
//      return new URLExecutionDataSource(file.toURL());
//    } catch (IOException e) {
//      throw new CoreException(EclEmmaStatus.EXEC_FILE_CREATE_ERROR.getStatus(e));
//    }
//  }

  /*
   * 
   * private Set<IPackageFragmentRoot> getSrcRoots(IJavaProject javaProject) {
   * IPackageFragmentRoot[] scope; try { scope =
   * javaProject.getAllPackageFragmentRoots();
   * 
   * Set<IPackageFragmentRoot> srcroot = new HashSet<IPackageFragmentRoot>();
   * for (final IPackageFragmentRoot root : scope) { IClasspathEntry entry; try
   * { entry = root.getRawClasspathEntry();
   * 
   * if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
   * 
   * srcroot.add(root); } catch (JavaModelException e) { // TODO Auto-generated
   * catch block e.printStackTrace(); }
   * 
   * } return srcroot; } catch (JavaModelException e1) { // TODO Auto-generated
   * catch block e1.printStackTrace(); } return null; }
   */
  /*
   * 
   * public String readString(InputStream inStream) throws IOException {
   * ByteArrayOutputStream outSteam = new ByteArrayOutputStream(); byte[] buffer
   * = new byte[1024]; int len = -1; while ((len = inStream.read(buffer)) != -1)
   * { outSteam.write(buffer, 0, len); } outSteam.close(); inStream.close();
   * return new String(outSteam.toByteArray());
   * 
   * }
   * 
   * private RangeDifference[] getDifferences(String s1, String s2) {
   * IRangeComparator comp1 = toRangeComparator(s1); IRangeComparator comp2 =
   * toRangeComparator(s2);
   * 
   * // CompareUIPlugin.getDefault().setUseOldDifferencer(false);
   * RangeDifference[] differences = RangeDifferencer.findDifferences(comp1,
   * comp2); // CompareUIPlugin.getDefault().setUseOldDifferencer(true); //
   * RangeDifference[] oldDifferences = //
   * RangeDifferencer.findDifferences(comp1, comp2); //
   * assertTrue(differences.length == oldDifferences.length); for (int i = 0; i
   * < differences.length; i++) { ConsoleMessage.showMessage("kind" +
   * differences[i].kind()); RangeDifference diff = differences[i];
   * ConsoleMessage.showMessage("diff:" + diff.leftStart() + "." +
   * diff.leftEnd() + "." + diff.rightStart() + "." + diff.rightEnd()); } return
   * differences; }
   * 
   * private IRangeComparator toRangeComparator(String s) { IDocument doc1 = new
   * Document(); doc1.set(s); return new DocLineComparator(doc1, null, true); }
   */

}
