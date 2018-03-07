/** */
package hu.qiang.filezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author huqiang */
public class FileZip {
	private static final int BUFFER_SIZE = 16*1024; // 16K
	private static final Logger logger = LoggerFactory.getLogger(FileZip.class);
	private boolean deleteCombinedFile = false;  //To leave the combined zip file, for investigation;
	private int compressMethod = ZipOutputStream.DEFLATED; //Default compressMethod;
	private int compressLevel = Deflater.DEFAULT_COMPRESSION; //Default compression level;
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
		if (!inDirectoryFile.exists()) {
			logger.error("Directory not found: {}", inDirectoryFile.getAbsolutePath() );
			return;
		}
		File outDirectoryFile = new File(outDirectory);
		try {
			createFolderIfNotExist(outDirectoryFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		Queue<File> directoryQueue = new LinkedList<>();
		directoryQueue.add(inDirectoryFile);
		String zipFileName;
		try {
			zipFileName = this.formZipFilePath(outDirectory, inDirectoryFile.getName());
		} catch (Exception e) {
			logger.error("Failed to get output zip name: " + e.getMessage(), e);
			return;
		}
		File zipFile = new File(zipFileName);

		try (FileOutputStream fileOutStream = new FileOutputStream(zipFile);
				ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);) {
			
			zipOutStream.setMethod(this.compressMethod);
			zipOutStream.setLevel(this.compressLevel);
			
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
	public void decompressDirectory(String inDirectory, String outDirectory) {
		logger.info("decompressDirectory: {}, {}", inDirectory, outDirectory);
		File inDirFile = new File(inDirectory);
		File[] parts = inDirFile.listFiles((dir, name) -> name.matches(".*\\.zip\\.p\\d$"));
		if (parts.length == 0) {
			logger.error("No parts file foud in dir: {}", inDirectory);
		}
		logger.info("{} part files found.", parts.length);
		String zipFileName = getZipFileName(parts[0]);
		File zipFile = null;
		try {
			FileUtil fu = new FileUtil();
			zipFile = fu.combineFile(new File(zipFileName));
		} catch (IOException e) {
			logger.error("Fail to combile zip file: " + zipFileName+ "-" + e.getMessage(), e);
		}
		try {
			if (zipFile == null) {
				logger.error("Fail to find zip file: {}", zipFileName);
				return;
			}
			this.decompressFile(zipFile, outDirectory);
			if (this.deleteCombinedFile) {
				Files.delete(zipFile.toPath());
			}
		} catch (Exception e) {
			logger.error("Fail to decpmpress zip file: " + zipFileName+ "-" + e.getMessage(), e);
		}
	}
	
	/**
	 * @param del
	 */
	public void setDeleteCombinedFile(boolean del) {
		this.deleteCombinedFile = del;
	}

	/**
	 * @param compressMethod
	 */
	public void setCompressMethod(int compressMethod) {
		this.compressMethod = compressMethod;
	}

	/**
	 * @param compressLevel
	 */
	public void setCompressLevel(int compressLevel) {
		this.compressLevel = compressLevel;
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
	 * @throws Exception 
	 */
	private void decompressFile(File inFile, String outDirectory) throws Exception {
		File outDirFile = new File(outDirectory);
		createFolderIfNotExist(outDirFile);

		byte[] buff = new byte[BUFFER_SIZE];
		ZipEntry entry = null;
		try (ZipFile zipFile = new ZipFile(inFile);
				ZipInputStream zipInput = new ZipInputStream(new FileInputStream(inFile));) {
			File outFile = null;
			while ((entry = zipInput.getNextEntry()) != null) {
				outFile = new File(outDirFile.getAbsolutePath() + File.separator + entry.getName());
				createFolderIfNotExist(outFile.getParentFile());
				
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
	 * @throws Exception 
	 */
	private String formZipFilePath(String dir, String name) throws Exception {
		File dirFile = new File(dir);
		createFolderIfNotExist(dirFile);
		return String.format("%s%s%s.zip", dirFile.getAbsolutePath(), File.separator, name);
	}
	

	/**
	 * @param folder
	 * @throws Exception
	 */
	private void createFolderIfNotExist(File folder) throws Exception {
		if (!folder.exists()) {
			boolean isFolderMade = false;
			try {
				isFolderMade = folder.mkdirs();
			} catch (Exception e) {
				logger.error("Failed to make folder: " + folder.getAbsolutePath() + "-" + e.getMessage(), e);
				throw e;
			}
			if (!isFolderMade) {
				throw new Exception("Failed to make folder: " + folder.getAbsolutePath());
			}
		}
	}
}
