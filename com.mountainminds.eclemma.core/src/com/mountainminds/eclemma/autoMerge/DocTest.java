package com.mountainminds.eclemma.autoMerge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

@SuppressWarnings("nls")
public class DocTest {

  public static void main(String arg[]) {
    test();
  }

  @SuppressWarnings("unused")
  public static boolean test() {
    File file = new File("d:\\temp\\1.java");
    File file2 = new File("d:\\temp\\2.java");
    // FileInputStream f = new FileInputStream(file);
    try {
      String str1 = readString(new FileInputStream(file));
      String str2 = readString(new FileInputStream(file2));

      RangeDifference[] s = getDifferences(str1, str2);

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private static RangeDifference[] getDifferences(String s1, String s2) {
    DocLineComparator comp1 = toRangeComparator(s1);
    DocLineComparator comp2 = toRangeComparator(s2);

    // CompareUIPlugin.getDefault().setUseOldDifferencer(false);
    RangeDifference[] differences = RangeDifferencer.findRanges(comp1, comp2);
    // CompareUIPlugin.getDefault().setUseOldDifferencer(true);
    // RangeDifference[] oldDifferences =
    // RangeDifferencer.findDifferences(comp1, comp2);
    // assertTrue(differences.length == oldDifferences.length);
    for (int i = 0; i < differences.length; i++) {
      ConsoleMessage.showMessage("kind" + differences[i].kind());
      RangeDifference diff = differences[i];
      ConsoleMessage.showMessage("diff:" + diff.leftStart() + "."
          + diff.leftEnd() + "." + diff.rightStart() + "." + diff.rightEnd());
    }
    return differences;
  }

  private static DocLineComparator toRangeComparator(String s) {
    IDocument doc1 = new Document();
    ConsoleMessage.showMessage("lines" + doc1.computeNumberOfLines(s));
    doc1.set(s);
    return new DocLineComparator(doc1, null, false);
  }

  public static String readString(InputStream inStream) throws IOException {
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
