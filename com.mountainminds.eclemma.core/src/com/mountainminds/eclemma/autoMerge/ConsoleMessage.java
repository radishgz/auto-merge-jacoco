package com.mountainminds.eclemma.autoMerge;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleMessage {

  static MessageConsole console;
  static MessageConsoleStream consoleStream;

  public ConsoleMessage() {

  }

  public static void showMessage(String msg) {
    if (console == null) {

      console = findConsole("Coverage Console"); //$NON-NLS-1$
      consoleStream = console.newMessageStream();
    }
    // 使用MessageConsoleStream来打印信息到Console View
    consoleStream.println(msg);
  }

  private static MessageConsole findConsole(String name) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++)
      if (name.equals(existing[i].getName()))
        return (MessageConsole) existing[i];
    // no console found, so create a new one
    MessageConsole myConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[] { myConsole });
    return myConsole;
  }

  static public void log(Exception ex) {
    // ILog log = TomcatLauncherPlugin.getDefault().getLog();
    StringWriter stringWriter = new StringWriter();
    ex.printStackTrace(new PrintWriter(stringWriter));
    String msg = stringWriter.getBuffer().toString();
    showMessage(msg);
    // Status status = new Status(IStatus.ERROR,
    // TomcatLauncherPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
    // IStatus.ERROR, msg, null);
    // log.log(status);
  }

}
