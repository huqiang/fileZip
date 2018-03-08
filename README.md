# FileZip
## Description
A simple program to compress folder, and decompress using JDK zip apis.  
When compressing, it takes in parameter: 'compression', 'FOLDER_TO_COMPRESS', 'OUTPUT_FOLDER', 'MAX_PART_SIZE_IN_MB', to compress the folder to produce splits with size <= the given `MAX_PART_SIZE_IN_MB`  
When decompressing, it takes in parameter: 'decompression', 'FOLDER_WITH_ZIPS', 'OUTPUT_FOLDER', to decompress the folder from the zip parts.  
## How to run
* To build:  
```
./gradlew build
```
* Simple run with gradle:  
```
./gradlew run -PappArgs="['compression','FOLDER_TO_COMPRESS','OUTPUT_FOLDER',2]"
./gradlew run -PappArgs="['decompression','FOLDER_WITH_ZIPS','OUTPUT_FOLDER']"
```
* Run with JVM heap size settings:
```
cd build/distributions/
unzip fileZip.zip 
APP_HOME=`pwd -P`
CLASSPATH=$APP_HOME/lib/file-zip-0.0.1.jar:$APP_HOME/lib/guava-23.0.jar:$APP_HOME/lib/logback-classic-1.2.3.jar:$APP_HOME/lib/slf4j-api-1.7.25.jar:$APP_HOME/lib/logback-core-1.2.3.jar:$APP_HOME/lib/jsr305-1.3.9.jar:$APP_HOME/lib/error_prone_annotations-2.0.18.jar:$APP_HOME/lib/j2objc-annotations-1.1.jar:$APP_HOME/lib/animal-sniffer-annotations-1.14.jar
java -Xms8m -Xmx8m -classpath $CLASSPATH hu.qiang.filezip.App compression /Users/huqiang/Downloads/testFileZip/in /Users/huqiang/Downloads/testFileZip/out 2
```
## Notes
* Symbolic Links are not supported.  
* Initial design of this program is:  
```
.
├── in
│   ├── fileA
│   └── fileB
└── out
    ├── fileA.zip
    ├── fileA_1.zip
    └── fileB.zip
```

While the correct implementation should be:  

```
.
├── in
│   ├── fileA
│   └── fileB
└── out
    ├── in.zip.p0
    ├── in.zip.p1
    └── in.zip.p2
```

* Some solutions for creating multi parts whose size is <= than MAX_SIZE:
    1. The maybe the easiest solution: to create a huge zip first, then split to multiple parts with size <= MAX_SIZE. This solution can ensure minimal number of splits, however hard to implement multi threading solution.  **THIS IS THE CURRENT IMPLEMENTING SOLUTION**  
    1. Select multiple source files to compress to a split with compressed size <= MAX_SIZE; Pro: 1, can concurrently compress, 2. each split can be decompressed individually, so can be concurrent; Cons: 1. hard to have the algo to for selecting source files so that min number of splits is created, 2. for source file compressed size larger than MAX_SIZE, then still need to further split it.   
    1. Compress source files one by one, then ZipOutputStream to write to zip parts with size <= MAX_SIZE. However, there is no existing API for it. One solution maybe ZipOutputStream to write to another OutputStream, the OutputStream tracking the size and output FileOutputStream to create output files whose size <= MAX_SIZE;  
* [ZipFileSystem](https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html) api shoud be better for implementing multi threading compression.

* GitHub Repo at: https://github.com/huqiang/fileZip


    