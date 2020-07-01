## Running MSC

### Installation of the required libraries
You need to download OpenJDK Version 11.0.2 from http://jdk.java.net/archive/ :
See instructions in "JDK download info.txt" in subfolder "jdk-11.0.2". Alternatively have a look at the [installation guide](https://github.com/zielesny/MSC/blob/master/Tutorial/MSC_Installation_Guide.pdf) for more information

### Running on various operating systems
- Windows: Double click the file *MSC_Windows.bat*
  ```
  MSC_Windows.bat
  ```
- Linux 
  ```
  $bash MSC_Linux.sh
  ```
- MacOS 
  ```
  $bash MSC_Mac.sh
  ```
### RAM defaults

- In order to have a smooth performance, the RAM allocation to the JVM is set to 4GB as a default.
If you want to start MSC with a different RAM specification, you have to edit one of the 
existing "MSC_\<OS\>" files.
- In the third row of each script, modify the option "-Xmx?g" where ? specifies the amount of RAM you want to allocate to the JVM in gigabytes.
### Getting started

- A short introductory tutorial for MSC usage is provided in the *Tutorial* folder.
