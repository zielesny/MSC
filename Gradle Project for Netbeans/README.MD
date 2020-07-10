## Gradle project
- This folder contains a Gradle project that can be compiled and run with a command-line interface or any IDE that supports Gradle. A JDK of version 11 or higher is needed on your machine. You don't need to have Gradle installed on your machine. This is done automatically by the Gradle-wrapper.

### IDEs
- The Gradle project can be opened using any of the IDEs that supports Gradle projects. For example the Netbeans IDE

### Command-line interface
The Gradle project can also be compiled using a command-line interface. In your command-line interface navigate into the “Gradle Project for Netbeans” 
folder. Use the following commands in your command-line to compile the tool:

- To compile MSC 
```
gradlew compileJava
```
- To execute MSC
```
gradlew run
```
- To test MSC
```
gradlew test
```
- To create a JAR file of MSC
```
gradlew jar
```
- To create an executable "fat" JAR file for MSC
```
gradlew fatJar
```
- To create MSC Javadoc
```
gradlew javadoc
```
- To delete the project build directory
```
gradlew clean
```
