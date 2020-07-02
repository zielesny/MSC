# Molecule Set Comparator (MSC)
The open rich-client Molecule Set Comparator (MSC) application enables a versatile and fast comparison of large molecule sets with a unique inter-set molecule-to-molecule mapping obtained e.g. by machine learning approaches. The molecule-to-molecule comparison is based on chemical descriptors obtained with the Chemistry Development Kit (CDK), such as Tanimoto similarities, atom/bond/ring counts or physicochemical properties like logP. The results are presented graphically and summarized by interactive histograms that can be exported in publication quality.

MSC is currently under scientific review - more information will be available soon.



## Contents of subfolders

- ***Tutorial*** - a guide for the installation of the MSC, a short introductory tutorial for MSC usage and an application example of the MSC

- ***src*** - all Java source files and resources

- ***lib*** - open libraries used by MSC

- ***installation*** - contains the installation folder *MSC_1.0* to be copied to a local machine for MSC execution. Note, that an additional JDK download is necessary (see [instructions](https://github.com/zielesny/MSC/blob/master/installation/MSC_1.0/jdk-11.0.2/JDK%20download%20info.txt) in subdirectory *jdk-11.0.2*). For a guided installation look at the [installation guide](https://github.com/zielesny/MSC/blob/master/Tutorial/MSC_Installation_Guide.pdf) in the *Tutorial* folder

- ***Gradle Project for Netbeans*** - a Gradle project that can be compiled and run with Gradle, the Netbeans IDE or any other IDE that supports Gradle (JDK 11 or higher is needed)

## Running MSC

- A precompiled [fat jar file](https://github.com/Kohulan/MSC/tree/master/installation/MSC_1.0/lib) (that includes all dependent libraries) is already provided in this repository. In order to directly use it, please have a look at the [steps](https://github.com/Kohulan/MSC/blob/master/installation/MSC_1.0/README.MD) provided under the installation directory. 
- Alternatively the MSC can be compiled and run using Gradle. For this, have a look at the *Gradle Project for Netbeans* folder where additional information is provided

## Notes
- MSC is not modularized due to its non-modularized library dependencies.
