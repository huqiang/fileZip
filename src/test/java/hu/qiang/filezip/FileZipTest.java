/**
 * 
 */
package hu.qiang.filezip;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author huqiang
 *
 */
public class FileZipTest {

	@Test
	@Ignore
	public void testCompressDirectory() {
		FileZip fz = new FileZip();
		fz.compressDirectory("/Users/huqiang/Downloads/testFileZip/in", "/Users/huqiang/Downloads/testFileZip/out", 10);
	}

	@Test
	@Ignore
	public void testCompressDirectoryWithSplit() throws Exception {
		FileZip fz = new FileZip();
		String outDirectory = "/Users/huqiang/Downloads/testFileZip/out";
		fz.compressDirectory("/Users/huqiang/Downloads/testFileZip/in", outDirectory, 3);
		File outDir = new File(outDirectory);
		// assertTrue("There should be 2 splits", outDir.listFiles().length == 2);
		// fz.splitFile(new File("/Users/huqiang/Downloads/testFileZip/out/big.zip"),
		// 3);
	}

	@Test
	// @Ignore
	public void testCombineFile() {
		FileZip fz = new FileZip();
		File zipFile = new File("/Users/huqiang/Downloads/testFileZip/out/big.zip");
		fz.combineFile(zipFile);
	}
}
