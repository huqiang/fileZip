package hu.qiang.filezip;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class FileUtilTest {

	@Test
	 @Ignore
	public void testCombineFile() {
		FileUtil fu = new FileUtil();
		File zipFile = new File("/Users/huqiang/Downloads/testFileZip/out/big.zip");
		try {
			fu.combineFile(zipFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
