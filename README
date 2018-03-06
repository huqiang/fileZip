# FileZip
## Description
## How to run
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
    ├── zip.zip
    ├── zip_1.zip
    └── zip_2.zip
```

* Three solutions for creating multi parts whose size is <= than MAX_SIZE:  
    1. Select source files to compress, however, it wont work for large source file, whose compressed size is > MAX_SIZE;  
    1. Compress source files one by one, then ZipOutputStream to write to zip parts with size <= MAX_SIZE. However, there is no existing API for it. One solution maybe ZipOutputStream to write to another OutputStream, the OutputStream tracking the size and output FileOutputStream to create output files whose size <= MAX_SIZE;  
    1. The maybe the easiest solution: to create a huge zip first, then split to multiple parts with size <= MAX_SIZE. 