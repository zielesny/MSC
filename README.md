# Molecule Set Comparator (MSC)
The open rich-client Molecule Set Comparator (MSC) application enables a versatile and fast comparison of large molecule sets with a unique inter-set molecule-to-molecule mapping obtained e.g. by machine learning approaches. The molecule-to-molecule comparison is based on chemical descriptors obtained with the Chemistry Development Kit (CDK), such as Tanimoto similarities, atom/bond/ring counts or physicochemical properties like logP. The results are presented graphically and summarized by interactive histograms that can be exported in publication quality.

MSC is currently under scientific review - more information will be available soon.



## Contents of subfolders

- ***Tutorial*** - short introductory PDF document for MSC usage

- ***src*** - all Java source files and resources

- ***lib*** - open libraries used by MSC

- ***installation*** - contains installation folder *MSC_1.0* to be copied to a local machine for MSC execution. Note, that an additional JDK download is necessary (see instructions in text file *JDK download info.txt* in subdirectory *jdk-11.0.2*)

- ***Gradle Project for Netbeans*** - Gradle project for Netbeans IDE (JDK 11 or higher)
  - To compile MSC, execute command *gradlew compileJava*
  - To test MSC, execute *gradlew test*
  - To create a JAR file of MSC, execute *gradlew jar*
  - To create an executable "fat" JAR file for MSC, execute *gradlew fatJar*
  - To create the MSC javadoc, execute *gradlew javadoc*
  - To run MSC, execute *gradlew run*
  - To delete the project build directory, execute *gradlew clean*


## Notes
- MSC is not modularized due to its non-modularized library dependencies.