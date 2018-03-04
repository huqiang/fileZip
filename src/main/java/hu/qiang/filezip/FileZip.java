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
  private static final int BUFFER_SIZE = 1048576; //1MB
  private static final Logger logger = LoggerFactory.getLogger(FileZip.class);

  public void compressDirectory(String inDirectory, String outDirectory, int maxSize) {
    logger.info(
        "compressDirectory: inDirectory = {}, outDirectory = {}, maxSize = {}",
        inDirectory,
        outDirectory,
        maxSize);
    Queue<File> directoryQueue = new LinkedList<>();
    directoryQueue.add(new File(inDirectory));
    while (!directoryQueue.isEmpty()) {
      File currentDirectory = directoryQueue.poll();
      String currentOutDirectory =
          this.formCurrentOutDirectory(currentDirectory, inDirectory, outDirectory);
      this.compressDirectory(currentDirectory, currentOutDirectory, maxSize, directoryQueue);
    }
  }

  public void decompressDirectory(String inDirectory, String outDirectory) {}

  private void compressFile(File inFile, String outDirectory, int maxSize) {
    logger.info(
        "compressFile: inFile = {}, outDirectory = {}, maxSize = {}",
        inFile.getAbsolutePath(),
        outDirectory,
        maxSize);

    if (!outDirectory.endsWith(File.separator)) {
      logger.debug("Appending seperator to outDirectory");
      outDirectory += File.separator;
    }
    int maxByteSize = maxSize * 1024 * 1024;
    long currentSize = 0;
    int zipSplitCount = 0;
    byte[] fileRAW = new byte[BUFFER_SIZE];
    String zipFileName = inFile.getName() + ".zip";
    ZipOutputStream zipOutStream = null;
    ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream(BUFFER_SIZE);
    ZipOutputStream sizeCalOutStream = new ZipOutputStream(byteArrayOutStream);
    FileInputStream inFileStream = null;
    try {
      File zipFile = new File(outDirectory + zipFileName);
      zipOutStream = new ZipOutputStream(new FileOutputStream(outDirectory + zipFileName));
      inFileStream = new FileInputStream(inFile);
      ZipEntry zipEntry;
      zipEntry = new ZipEntry(inFile.getName());
      zipOutStream.putNextEntry(zipEntry);
      sizeCalOutStream.putNextEntry(zipEntry);

      int count;
      while ((count = inFileStream.read(fileRAW, 0, BUFFER_SIZE)) != -1) {
        byteArrayOutStream.reset();
        sizeCalOutStream.write(fileRAW, 0, count);
        int incrementalSize = byteArrayOutStream.size();
        logger.debug("incrementalSize: {}", incrementalSize);
        zipOutStream.write(fileRAW, 0, count);
        logger.debug("Current zipFile size is: {}", zipFile.length());
        if ((currentSize + incrementalSize)>= maxByteSize) {
          logger.info("currentSize: {} is larger than maxByteSize: {}", currentSize, maxByteSize);
          zipSplitCount++;
          zipOutStream.close();
          zipOutStream =
              new ZipOutputStream(
                  new FileOutputStream(
                      outDirectory + zipFileName.replace(".zip", "_" + zipSplitCount + ".zip")));
          currentSize = 0;
        }
        currentSize += incrementalSize;
        logger.debug("currentSize: {}", currentSize);
        logger.debug("size diff: {}", zipFile.length() - currentSize);
      }
      inFileStream.close();
      zipOutStream.closeEntry();

      zipOutStream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (zipOutStream != null) zipOutStream.close();
        if (inFileStream != null) inFileStream.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private void decompressFile(File inFile, String outDirectory) {}

  private void compressDirectory(
      File currentDirectory, String currentOutDirectory, int maxSize, Queue<File> directoryQueue) {
    File currentOutDir = new File(currentOutDirectory);
    currentOutDir.mkdir();
    for (File f : currentDirectory.listFiles()) {
      if (f.isDirectory()) {
        directoryQueue.add(f);
      } else {
        compressFile(f, currentOutDirectory, maxSize);
      }
    }
  }

  private String formCurrentOutDirectory(File dir, String inDirectory, String outDirectory) {
    return dir.getPath().replaceFirst(inDirectory, outDirectory);
  }
}
