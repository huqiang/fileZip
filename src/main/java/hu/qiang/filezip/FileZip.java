/** */
package hu.qiang.filezip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author huqiang */
public class FileZip {
	private static final int BUFFER_SIZE = 1048576; // 1MB
	private static final Logger logger = LoggerFactory.getLogger(FileZip.class);
	private static final String BIG_ZIP_NAME = "big.zip";

	public void compressDirectory(String inDirectory, String outDirectory, int maxSize) {
		logger.info("compressDirectory: inDirectory = {}, outDirectory = {}, maxSize = {}", inDirectory, outDirectory,
				maxSize);
		File inDirectoryFile = new File(inDirectory);
		File outDirectoryFile = new File(outDirectory);
		if (!outDirectoryFile.exists()) {
			outDirectoryFile.mkdir();
		}
		Queue<File> directoryQueue = new LinkedList<>();
		directoryQueue.add(inDirectoryFile);
		String zipFileName = this.formZipFilePath(outDirectory, BIG_ZIP_NAME);
		File zipFile = new File(zipFileName);

		try (FileOutputStream fileOutStream = new FileOutputStream(zipFile);
				ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);) {
			while (!directoryQueue.isEmpty()) {
				File currentDirectory = directoryQueue.poll();
				this.compressDirectory(inDirectory, currentDirectory, zipOutStream, directoryQueue);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Final zip file size: {}", zipFile.length());
		try {
			this.splitFile(zipFile, maxSize);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void decompressDirectory(String inDirectory, String outDirectory) {
	}

	private void compressFile(String inDir, File inFile, ZipOutputStream zipOutStream)
			throws FileNotFoundException, IOException {
		logger.info("compressFile: inDir={}, inFile = {}, maxSize = {}", inDir, inFile.getAbsolutePath());

		byte[] fileRAW = new byte[BUFFER_SIZE];
		try (FileInputStream inFileStream = new FileInputStream(inFile)) { // Auto closed
			ZipEntry zipEntry;
			zipEntry = new ZipEntry(inFile.getAbsolutePath().replaceFirst(inDir, ""));
			logger.debug(zipEntry.getName());
			zipOutStream.putNextEntry(zipEntry);
			int count;
			while ((count = inFileStream.read(fileRAW, 0, BUFFER_SIZE)) != -1) {
				zipOutStream.write(fileRAW, 0, count);
			}
			zipOutStream.closeEntry();
		}
	}

	private void decompressFile(File inFile, String outDirectory) {
	}

	private void compressDirectory(String inDir, File currentDirectory, ZipOutputStream zipOutStream,
			Queue<File> directoryQueue) throws FileNotFoundException, IOException {
		File zippedFile = new File("/Users/huqiang/Downloads/testFileZip/out/big.zip");
		logger.debug("Zip file size before adding directory: {}", zippedFile.length());
		for (File f : currentDirectory.listFiles()) {
			if (f.isDirectory()) {
				directoryQueue.add(f);
			} else {
				compressFile(new File(inDir).getAbsolutePath(), f, zipOutStream);
				logger.debug("Zip file size after adding {}: {}", f.getAbsolutePath(), zippedFile.length());
			}
		}
	}

	public void splitFile(File file, int maxSize) throws FileNotFoundException {
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
				// logger.debug("Wrote {} bytes", readsize);
				fos.flush();
				pos += readsize;
			}
			file.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void combineFile(File file) {
		String filename = file.getAbsolutePath();
		File fileout = new File(filename);

		try (FileOutputStream fos = new FileOutputStream(fileout);) {
			int splitCount = 0;
			File filein = new File(filename + ".p" + splitCount);
			FileInputStream fis = null; // = new FileInputStream(filein);
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
				if (fis != null) {
					fis.close();
				}
				filein = new File(filename + ".p" + (++splitCount));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String formZipFilePath(String dir, String name) {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return String.format("%s%s%s", dirFile.getAbsolutePath(), File.separator, name);
	}
}
