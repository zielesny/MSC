## Running MSC

### Installation of the required libraries
You need to download OpenJDK Version 11.0.2 from http://jdk.java.net/archive/ :
See instructions in "JDK download info.txt" in subfolder "jdk-11.0.2"

### Running on various operate systems
- Windows : double click the MSC_Windows.bat
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
### RAM Defaults

- In order to have a smooth performance the RAM allocation to JVM is set to 4GB in default.
If you want to start MSC with a different RAM specification, you have to edit one of the 
existing "MSC_<OS>" file.
- In the third row of each script, modify option "-Xmx?g" where ? specifies the amount of RAM you want to allocate for the JVM in gigabytes.

- A short and comprehensive introduction to MSC is provided in the "Tutorial" folder.
