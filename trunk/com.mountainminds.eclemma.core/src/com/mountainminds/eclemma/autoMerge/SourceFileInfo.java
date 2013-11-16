package com.mountainminds.eclemma.autoMerge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.LineImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.runtime.WildcardMatcher;

import com.mountainminds.eclemma.internal.core.analysis.PackageFragementRootAnalyzer;

@SuppressWarnings("nls")
public class SourceFileInfo {

  private String className;
  private IFile javafile;
  private int fristLine;
  private int lastLine;
  private LineImpl[] lines;
  private String packageFragmentRoot;
  private String filetimestamp;
  private String oldfileContent;

  public String getOldfileContent() {
    return oldfileContent;
  }

  public String getFiletimestamp() {
    return filetimestamp;
  }

  public void setFiletimestamp(String filetimestamp) {
    this.filetimestamp = filetimestamp;
  }

  public String getPackageFragmentRoot() {
    return packageFragmentRoot;
  }

  public void setPackageFragmentRoot(String packageFragmentRoot) {
    this.packageFragmentRoot = packageFragmentRoot;
  }

  public String getJavafilename() {
    return javafilename;
  }

  public void setJavafilename(String javafilename) {
    this.javafilename = javafilename;
  }

  private String javafilename;

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public IFile getJavafile() {
    return javafile;
  }

  public void setJavafile(IFile javafile) {
    this.javafile = javafile;
  }

  public int getFristLine() {
    return fristLine;
  }

  public void setFristLine(int fristLine) {
    this.fristLine = fristLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  public void setLastLine(int lastLine) {
    this.lastLine = lastLine;
  }

  public LineImpl[] getLines() {
    return lines;
  }

  public void setLines(LineImpl[] lines) {
    this.lines = lines;
  }

  public SourceFileInfo(String className, IFile javafile, String javafilename,
      String filetimestamp) {
    super();
    this.className = className;
    this.javafile = javafile;
    this.javafilename = javafilename;
    this.filetimestamp = filetimestamp;
  }

  public SourceFileInfo() {
    // TODO Auto-generated constructor stub
  }

  private final static String sp = ",";

  public void saveToFile() throws IOException, CoreException {

    IPath ProjectRoot = LauncherInfo.getJavaproject().getProject()
        .getLocation().makeAbsolute();
    // System.err.println("file:" + ProjectRoot.toOSString());

    Properties FileProperties = new Properties();
    FileProperties.put("className", className);
    FileProperties
        .put("javafile", javafile.getProjectRelativePath().toString());
    FileProperties.put("packageFragmentRoot", packageFragmentRoot);

    FileProperties.put("javafilename", javafilename);
    FileProperties.put("fristLine", String.valueOf(fristLine));
    FileProperties.put("lastLine", String.valueOf(lastLine));
    FileProperties.put("filetimestamp", this.filetimestamp);
    for (int i = 0; i < lines.length; i++) {
      // LineImpl line=;
      if (lines[i] == null)
        continue;
      CounterImpl impl = (CounterImpl) lines[i].getInstructionCounter();

      String line = impl.getMissed() + sp + impl.getCovered();
      impl = (CounterImpl) lines[i].getBranchCounter();
      line += sp + impl.getMissed() + sp + impl.getCovered();
      FileProperties.put("Line" + (i + fristLine), line);
    }
    File file = ProjectRoot.append(
        JacocoData.DIRNAME + "/" + LauncherInfo.ProjectName + "/"
            + javafilename.replace(".java", ".properties")).toFile();
    // System.err.println("file:" + file.getAbsolutePath());
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    FileOutputStream output = new FileOutputStream(file);
    FileProperties.store(output, "auto write by jacococ-extion");
    // 保持javafile内容，以便后续比较
    InputStream content = javafile.getContents();
    String javaContent = readString(content);
    file = ProjectRoot.append(
        JacocoData.DIRNAME + "/" + LauncherInfo.ProjectName + "/"
            + javafilename).toFile();
    output = new FileOutputStream(file);
    output.write(javaContent.getBytes());
    output.close();
    /*
     * File tempfile = new File("d:/temp/" + javafilename.replace(".java",
     * ".properties").replace("/", "_") + filetimestamp); FileOutputStream
     * tempput = new FileOutputStream(tempfile);
     * 
     * FileProperties.store(tempput, "auto write by jacococ-extion");
     */
  }

  public static String getNow() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddHHmmss");
    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
    String str = formatter.format(curDate);
    return str;
  }

  public static SourceFileInfo loadFromFile(File file) throws IOException,
      JavaModelException {

    // IPath BinRoot = LauncherInfo.getJavaproject().getOutputLocation();
    IResource ProjectRoot = LauncherInfo.getJavaproject().getResource();

    Properties FileProperties = new Properties();
    FileInputStream input = new FileInputStream(file);
    FileProperties.load(input);

    String className = FileProperties.getProperty("className");
    IPath javafilepath = ProjectRoot.getProjectRelativePath().append(
        FileProperties.getProperty("javafilename"));

    // String path = fullclassName.replace('/', IPath.SEPARATOR) + ".class";
    // IPath packagePath = BinRoot.append(path);
    IFile javafile = (IFile) ProjectRoot.getProject().findMember(javafilepath);

    String javafilename = FileProperties.getProperty("javafilename");
    String filetimestamp = FileProperties.getProperty("filetimestamp");
    SourceFileInfo fileinfo = new SourceFileInfo(className, javafile,
        javafilename, filetimestamp);

    int fristLine = Integer.parseInt(FileProperties.getProperty("fristLine"));
    int lastLine = Integer.parseInt(FileProperties.getProperty("lastLine"));

    fileinfo.setFristLine(fristLine);
    fileinfo.setLastLine(lastLine);
    LineImpl[] lines = new LineImpl[lastLine - fristLine + 1];
    for (int i = fristLine, j = 0; i <= lastLine; i++, j++) {
      String temp = FileProperties.getProperty("Line" + i);
      if (temp == null) {
        lines[j] = LineImpl.EMPTY;
        continue;
      }
      String counter[] = temp.split(sp);
      int missed = Integer.parseInt(counter[0]);
      int covered = Integer.parseInt(counter[1]);
      CounterImpl instructions = CounterImpl.getInstance(missed, covered);
      missed = Integer.parseInt(counter[2]);
      covered = Integer.parseInt(counter[3]);
      CounterImpl branches = CounterImpl.getInstance(missed, covered);

      lines[j] = LineImpl.getInstance(instructions, branches);

    }
    fileinfo.setLines(lines);

    fileinfo.setPackageFragmentRoot(FileProperties
        .getProperty("packageFragmentRoot"));
    File OldJavaFile = new File(file.getAbsolutePath().replace(".properties",
        ".java"));
    if (!OldJavaFile.exists())
      return null;

    FileInputStream javafileinput = new FileInputStream(OldJavaFile);
    fileinfo.oldfileContent = readString(javafileinput);
    javafileinput.close();
    return fileinfo;
  }

  public static String getSp() {
    return sp;
  }

  // private HashMap<String, SourceFileInfo> oldFiles = new HashMap<String,
  // SourceFileInfo>();

  /**
   * 只装载设置coverage的类
   * 
   * @param oldFiles
   *          结果map
   * @param rootName
   *          根目录
   * @param includes
   *          要包括的类
   * @param excludes
   *          不包括的类
   * @return
   * @throws JavaModelException
   * @throws IOException
   */
  public HashMap<String, SourceFileInfo> loadAllFile(String rootName,
      String includes, String excludes) throws JavaModelException, IOException {
    WildcardMatcher includesMactcher = new WildcardMatcher(includes);
    WildcardMatcher excludesMactcher = new WildcardMatcher(excludes);
    File root = new File(JacocoData.getJacocoDataDir() + rootName);
    HashMap<String, SourceFileInfo> old = new HashMap<String, SourceFileInfo>();
    loadAllFile(root, old, root.getAbsolutePath().length(), includesMactcher,
        excludesMactcher);
    return old;
  }

  private HashMap<String, SourceFileInfo> loadAllFile(File file,
      HashMap<String, SourceFileInfo> oldFiles, int rootLength,
      WildcardMatcher includesMactcher, WildcardMatcher excludesMactcher)
      throws JavaModelException, IOException {

    if (file.isDirectory()) {
      // Directory dic=file;
      File[] children = file.listFiles();
      for (File child : children) {
        if (child.isDirectory()) {
          loadAllFile(child, oldFiles, rootLength, includesMactcher,
              excludesMactcher);
        } else {

          String filePath = child.getAbsolutePath();
          filePath = filePath.substring(rootLength + 1).replace(
              File.separatorChar, '.');
          // ConsoleMessage.showMessage("filename:" + child.getAbsolutePath()
          // + "--" + filePath);
          if (!filePath.endsWith(".properties"))
            continue;
          filePath = filePath.replace(".properties", "");
          if (includesMactcher.matches(filePath)
              && !excludesMactcher.matches(filePath)) {

            SourceFileInfo fileinfo = SourceFileInfo.loadFromFile(child);
            if (fileinfo != null) {
              oldFiles.put(fileinfo.getJavafilename(), fileinfo);
              ConsoleMessage.showMessage("add old filename:"
                  + child.getAbsolutePath());
            }
          }

        }
      }
    }
    return oldFiles;

  }

  public HashMap<String, SourceFileInfo> loadFromPackageFragementRootAnalyzer(
      PackageFragementRootAnalyzer rootAnaLyzer, IPackageFragmentRoot root)
      throws CoreException {
    HashMap<String, SourceFileInfo> sessionSourceFiles = new HashMap<String, SourceFileInfo>();

    Collection<ISourceFileCoverage> sourceFilesCoverage = rootAnaLyzer
        .getCache().get(getClassfilesLocation(root)).getSourcemap().values();
    for (ISourceFileCoverage coverage : sourceFilesCoverage) {
      if (!(coverage instanceof SourceFileCoverageImpl))
        continue;
      SourceFileCoverageImpl sourceCoverage = (SourceFileCoverageImpl) coverage;
      String className = coverage.getPackageName() + "/"
          + sourceCoverage.getName();
      IPackageFragment packageFragment = root.getPackageFragment(coverage
          .getPackageName());
      ICompilationUnit unit = packageFragment.getCompilationUnit(sourceCoverage
          .getName());
      IFile javaFile = (IFile) unit.getResource();
      if (javaFile.exists()) {
        IPath path = unit.getPath();
        path = path.makeRelativeTo(root.getJavaProject().getPath());
        String filetimestamp = String.valueOf(javaFile.getLocalTimeStamp());
        SourceFileInfo sourcefile = new SourceFileInfo(className, javaFile,
            path.toString(), filetimestamp);
        sourcefile.setPackageFragmentRoot(root.getPath().toString());
        sourcefile.setLines(sourceCoverage.getLines());
        sourcefile.setFristLine(sourceCoverage.getFirstLine());
        sourcefile.setLastLine(sourceCoverage.getLastLine());
        sessionSourceFiles.put(path.toString(), sourcefile);
      }
    }
    return sessionSourceFiles;

  }

  static IResource getClassfilesLocation(IPackageFragmentRoot root)
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

  private static String readString(InputStream inStream) throws IOException {
    ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len = -1;
    while ((len = inStream.read(buffer)) != -1) {
      outSteam.write(buffer, 0, len);
    }
    outSteam.close();
    inStream.close();
    return new String(outSteam.toByteArray());

  }

}
