/**
 * 
 */
package hu.qiang.filezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huqiang
 *
 */
public class FileUtil {
	private static final int BUFFER_SIZE = 16*1024; // 16K
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	
	/**
	 * Method to split given file to parts with size <= given maxSize
	 * @param file: the file to be split
	 * @param maxSize: max size of split parts in MB
	 * @throws IOException
	 */
	public void splitFile(File file, int maxSize) throws IOException {
		logger.info("Splitting {} with size {}MB to with part size: {}MB", file.getName(), file.length() / 1024 / 1024,
				maxSize);
		if (maxSize <= 0) {
			maxSize = 1;
		}
		int maxByteSize = maxSize * 1024 * 1024;
		logger.debug("File size: {}", file.length());
		logger.info("Splitting to {} parts", (file.length() / maxByteSize) + 1);

		if (!file.isFile()) {
			throw new FileNotFoundException("file not exists" + file.getAbsolutePath());
		}

		String filename = file.getAbsolutePath();
		FileOutputStream fos = null;
		try (FileInputStream fis = new FileInputStream(file);) {
			byte[] buf = new byte[BUFFER_SIZE];
			int readsize = 0;
			int pos = 0;
			int splitCount = 0;
			File fileout = new File(filename + ".p" + splitCount);
			fos = new FileOutputStream(fileout);
			while ((readsize = fis.read(buf, 0, BUFFER_SIZE)) > 0) {
				if (pos + readsize > maxByteSize) {
					fos.close();
					fileout = new File(filename + ".p" + (++splitCount));
					fos = new FileOutputStream(fileout);
					pos = 0;
				}
				fos.write(buf, 0, readsize);
				fos.flush();
				pos += readsize;
			}
			Files.delete(file.toPath());
		} catch (IOException e) {
			logger.error("Failed to splitFile: " + file.getAbsolutePath(), e);
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	
	/**
	 * Method to combine parts to generate the given file.
	 * @param file
	 * @return file
	 * @throws IOException
	 */
	public File combineFile(File file) throws IOException {
		String filename = file.getAbsolutePath();
		File fileout = new File(filename);

		FileInputStream fis = null;
		try (FileOutputStream fos = new FileOutputStream(fileout);) {
			int splitCount = 0;
			File filein = new File(filename + ".p" + splitCount);
			byte[] buf = new byte[BUFFER_SIZE];
			while (filein.isFile()) {
				logger.debug("Reading file: {}", filein.getName());
				fis = new FileInputStream(filein);
				int readsize = 0;
				while ((readsize = fis.read(buf, 0, BUFFER_SIZE)) > 0) {
					logger.debug("Write {} bytes.", readsize);
					fos.write(buf, 0, readsize);
					fos.flush();
				}
				fis.close();
				filein = new File(filename + ".p" + (++splitCount));
			}
		} catch (IOException e) {
			logger.error("Failed to combineFile: " + file.getAbsolutePath(), e);
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return fileout;
	}

}
