/** */
package hu.qiang.filezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author huqiang */
public class FileZip {
	private static final int BUFFER_SIZE = 1048576; // 1MB
	private static final Logger logger = LoggerFactory.getLogger(FileZip.class);

	/**
	 * To compress the inDirectory to set of zip with size <= maxSize, to outDirectory
	 * @param inDirectory
	 * @param outDirectory
	 * @param maxSize
	 */
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
		String zipFileName = this.formZipFilePath(outDirectory, inDirectoryFile.getName());
		File zipFile = new File(zipFileName);

		try (FileOutputStream fileOutStream = new FileOutputStream(zipFile);
				ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);) {
			while (!directoryQueue.isEmpty()) {
				File currentDirectory = directoryQueue.poll();
				this.compressDirectory(inDirectory, currentDirectory, zipOutStream, directoryQueue);
			}
		} catch (IOException e) {
			logger.error("Fail to compress directory: " + inDirectory+ "-" + e.getMessage(), e);
		}
		logger.debug("Final zip file size: {}", zipFile.length());
		try {
			FileUtil fu = new FileUtil();
			fu.splitFile(zipFile, maxSize);
		} catch (IOException e) {
			logger.error("Failed to split zip file: " + zipFile.getAbsolutePath()+ "-" + e.getMessage(), e);
		}
	}

	/**
	 * @param inDirectory
	 * @param outDirectory
	 * @throws IOException
	 */
	public void decompressDirectory(String inDirectory, String outDirectory) throws IOException {
		logger.info("decompressDirectory: {}, {}", inDirectory, outDirectory);
		File inDirFile = new File(inDirectory);
		File[] parts = inDirFile.listFiles((dir, name) -> name.matches(".*\\.zip\\.p\\d$"));
		if (parts.length == 0) {
			throw new FileNotFoundException("No parts file foud in dir: " + inDirectory);
		}
		logger.info("{} part files found.", parts.length);
		String zipFileName = getZipFileName(parts[0]);
		File zipFile;
		try {
			FileUtil fu = new FileUtil();
			zipFile = fu.combineFile(new File(zipFileName));
		} catch (IOException e) {
			logger.error("Fail to combile zip file: " + zipFileName+ "-" + e.getMessage(), e);
			throw e;
		}
		this.decompressFile(zipFile, outDirectory);
	}

	/**
	 * To get the zip file name from the part file;
	 * @param partFile part file of zip
	 * @return zip file absolute string path
	 */
	private String getZipFileName(File partFile) {
		return partFile.getAbsolutePath().substring(0, partFile.getAbsolutePath().lastIndexOf(".p"));
	}

	/**
	 * Method to decompress zip file to given directory
	 * @param inFile: the zip file to be decompressed
	 * @param outDirectory: the decompressed to put
	 * @throws IOException
	 */
	private void decompressFile(File inFile, String outDirectory) throws IOException {
		File outDirFile = new File(outDirectory);
		if (!outDirFile.exists()) {
			outDirFile.mkdirs();
		}

		byte[] buff = new byte[BUFFER_SIZE];
		ZipEntry entry = null;
		try (ZipFile zipFile = new ZipFile(inFile);
				ZipInputStream zipInput = new ZipInputStream(new FileInputStream(inFile));) {
			File outFile = null;
			while ((entry = zipInput.getNextEntry()) != null) {
				outFile = new File(outDirFile.getAbsolutePath() + File.separator + entry.getName());
				if (!outFile.getParentFile().exists()) {
					outFile.getParentFile().mkdirs();
				}
				if (!outFile.exists()) {
					outFile.createNewFile();
				}
				try (InputStream input = zipFile.getInputStream(entry);
						OutputStream out = new FileOutputStream(outFile);) {
					int readSize = 0;
					while ((readSize = input.read(buff, 0, BUFFER_SIZE)) > 0) {
						out.write(buff, 0, readSize);
					}
				}
			}
		}
	}

	/**
	 * Compress (sub) directory 
	 * @param rootDirectory: the root directory of the compression
	 * @param currentDirectory: current directory to be processed
	 * @param zipOutStream: ZipOutputStream to write out compressed bytes
	 * @param directoryQueue: queue to track scanned sub directories
	 * @throws IOException
	 */
	private void compressDirectory(String rootDirectory, File currentDirectory, ZipOutputStream zipOutStream,
			Queue<File> directoryQueue) throws IOException {
		for (File f : currentDirectory.listFiles()) {
			if (f.isDirectory()) {
				directoryQueue.add(f);
			} else {
				compressFile(new File(rootDirectory).getAbsolutePath(), f, zipOutStream);
			}
		}
	}
	

	/**
	 * Method to compress given file, based on the root directory
	 * @param rootDirectory
	 * @param inFile
	 * @param zipOutStream
	 * @throws IOException
	 */
	private void compressFile(String rootDirectory, File inFile, ZipOutputStream zipOutStream)
			throws IOException {
		logger.info("compressFile: inDir={}, inFile = {}", rootDirectory, inFile.getAbsolutePath());

		byte[] fileRAW = new byte[BUFFER_SIZE];
		try (FileInputStream inFileStream = new FileInputStream(inFile)) { // Auto closed
			ZipEntry zipEntry;
			zipEntry = new ZipEntry(inFile.getAbsolutePath().replaceFirst(rootDirectory, ""));
			zipOutStream.putNextEntry(zipEntry);
			int count;
			while ((count = inFileStream.read(fileRAW, 0, BUFFER_SIZE)) > 0) {
				zipOutStream.write(fileRAW, 0, count);
			}
			zipOutStream.closeEntry();
		}
	}

	/**
	 * Form absolute path of the zip file of given directory and file name;
	 * @param dir: the directory to put zip file
	 * @param name: name of the zip file, without extension
	 * @return the absolute string path of the zip file
	 */
	private String formZipFilePath(String dir, String name) {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return String.format("%s%s%s.zip", dirFile.getAbsolutePath(), File.separator, name);
	}
}
