package com.sysdeo.eclipse.tomcat.actions;

/*
 * (c) Copyright Sysdeo SA 2001, 2002.
 * All Rights Reserved.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

import com.mountainminds.eclemma.autoMerge.LauncherInfo;
import com.mountainminds.eclemma.core.CoverageTools;
import com.mountainminds.eclemma.core.IExecutionDataSource;
import com.mountainminds.eclemma.core.ISessionManager;
import com.mountainminds.eclemma.internal.core.CoverageSession;
import com.mountainminds.eclemma.internal.core.ExecutionDataFiles;
import com.mountainminds.eclemma.internal.core.MemoryExecutionDataSource;
import com.mountainminds.eclemma.internal.core.SessionImporter;
import com.sysdeo.eclipse.tomcat.TomcatBootstrap;
import com.sysdeo.eclipse.tomcat.TomcatLauncherPlugin;

public class StopActionDelegate implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/*
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (TomcatLauncherPlugin.checkTomcatSettingsAndWarn()) {
			// TomcatLauncherPlugin.log(TomcatLauncherPlugin.getResourceString("msg.stop"));
			try {

				dumpJacoco();

				TomcatLauncherPlugin.getDefault().getTomcatBootstrap().stop();
				TomcatLauncherPlugin.log("tomcat stop");

			} catch (Exception ex) {
				String msg = TomcatLauncherPlugin
						.getResourceString("msg.stop.failed");
				TomcatLauncherPlugin.log(msg + "/n");
				TomcatLauncherPlugin.log(ex);
			}
		}
	}

	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private static final String DESTFILE = "d:/temp/jacoco-client.exec";

	private static final String ADDRESS = "localhost";

	// private static final int PORT = 8999;

	public static void dumpJacoco() throws IOException {

		ExecutionDataFiles executionDataFiles = new ExecutionDataFiles();

		// final FileOutputStream localFile = new FileOutputStream(DESTFILE);
		// final ExecutionDataWriter localWriter = new ExecutionDataWriter(
		// localFile);
		int port = TomcatLauncherPlugin.getDefault().getPreferenceStore()
				.getInt(TomcatLauncherPlugin.JacocoAgentPort);
		// Open a socket to the coverage agent:
		final Socket socket = new Socket(InetAddress.getByName(ADDRESS), port);
		final RemoteControlWriter writer = new RemoteControlWriter(
				socket.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(
				socket.getInputStream());

		MemoryExecutionDataSource memoryExecutionDataSource = new MemoryExecutionDataSource();

		reader.setSessionInfoVisitor(memoryExecutionDataSource);
		reader.setExecutionDataVisitor(memoryExecutionDataSource);

		// reader.setSessionInfoVisitor(localWriter);
		// reader.setExecutionDataVisitor(localWriter);

		// Send a dump command and read the response:
		writer.visitDumpCommand(true, false);
		reader.read();

		socket.close();

		try {
			IExecutionDataSource source = executionDataFiles
					.newFile(memoryExecutionDataSource);
			ISessionManager sessionManager = CoverageTools.getSessionManager();
			ILaunchConfiguration launchconfig = null;
			CoverageSession Coveragesession = new CoverageSession(
					"tomcat coverage"+getNow(), TomcatBootstrap.getSrcRoots(), source,
					launchconfig);
			// SessionImporter importer=new
			// SessionImporter(sessionManager,executionDataFiles);
			// importer.importSession(s);
			// ICoverageSession session=
			ILaunch launch = null;
			//setIjavaProject
			
			 
					
			sessionManager.addSession(Coveragesession, true, launch);
			
		} catch (CoreException e) {
			TomcatLauncherPlugin.log(e);
		}

		// localFile.close();
	}

	public static String getNow() {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}
}
