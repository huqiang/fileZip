/**
 * 
 */
package hu.qiang.filezip;

import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Test;

/**
 * @author huqiang
 *
 */
public class FileZipTest {

  @Test
  public void testCompressDirectory(){
    FileZip fz = new FileZip();
    fz.compressDirectory("/Users/huqiang/Downloads/testFileZip/in", "/Users/huqiang/Downloads/testFileZip/out", 10);
  }
  
  @Test
  public void testCompressDirectoryWithSplit(){
    FileZip fz = new FileZip();
    String outDirectory = "/Users/huqiang/Downloads/testFileZip/out";
    fz.compressDirectory("/Users/huqiang/Downloads/testFileZip/in", outDirectory, 3);
    File outDir = new File(outDirectory);
    assertTrue("There should be 2 splits", outDir.listFiles().length == 2);
  }

}
