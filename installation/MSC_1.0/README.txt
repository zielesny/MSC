You need to download OpenJDK Version 11.0.2 from http://jdk.java.net/archive/ :
See instructions in "JDK download info.txt" in subfolder "jdk-11.0.2"

You can start MSC by executing one of the "MSC_?GB_<OS>" files, where <OS> should 
be your operating system and ? specifies the number of gigabytes of RAM that is 
allocated for the JVM.

If you want to start MSC with a different RAM specification, you have to edit one of the 
existing "MSC_?GB_<OS>" files (or a copy of them). In the third row, modify option
"-Xmx?g" where ? specifies the amount of RAM you want to allocate for the JVM in gigabytes.

A short and comprehensive introduction to MSC is provided in the "Tutorial" folder.
