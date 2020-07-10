# Molecule Set Comparator (MSC)
- Molecule Set Comparator (MSC) is designed as an application that enables a user to do a versatile and fast comparison of large molecule sets with a unique inter-set, molecule-to-molecule comparison, for the original set and for a predicted set of molecules obtained by machine learning approaches. 

- The molecule-to-molecule comparison is based on chemical descriptors, which are included in the Chemistry Development Kit (CDK), such as Tanimoto similarities, atom/bond/ring counts, and physicochemical properties like logP. The results are presented graphically and summarized by interactive histograms that can be exported in publication quality


## Contents of subfolders

- ***Tutorial*** - a comprehensive step-by-step guide for the installation of the MSC and a short introductory tutorial for MSC usage and an application example of the MSC

- ***src*** - all Java source files and resources

- ***lib*** - open libraries used by MSC

- ***installation*** - contains the installation folder *MSC_1.0* to be copied to a local machine for MSC execution. 
  - Note, that an additional JDK download is necessary (see [instructions](https://github.com/zielesny/MSC/blob/master/installation/MSC_1.0/jdk-11.0.2/JDK%20download%20info.txt) in subdirectory *JDK-11.0.2*). 
  
  - For a guided installation look at the [installation guide](https://github.com/zielesny/MSC/blob/master/Tutorial/MSC_Installation_Guide.pdf) in the *Tutorial* folder

- ***Gradle Project for Netbeans*** - a Gradle project that can be compiled and run with Gradle, the Netbeans IDE or any other IDE that supports Gradle (JDK 11 or higher is needed)

## Running MSC

- A precompiled [fat jar file](https://github.com/Kohulan/MSC/tree/master/installation/MSC_1.0/lib) (that includes all dependent libraries) is already provided in this repository. To directly use it, please have a look at the [steps](https://github.com/Kohulan/MSC/blob/master/installation/MSC_1.0/README.MD) provided under the installation directory. 

- Alternatively, the MSC can be compiled using Gradle. For this, have a look at the *Gradle Project for Netbeans* folder where additional information is provided

## Citing the tool

- MSC is currently under scientific review - more information will be available soon.

## Notes
- MSC is not modularized due to its non-modularized library dependencies.

## MSC
![GitHub Logo](https://github.com/Kohulan/MSC/blob/master/assets/MSC.png?raw=true)

