## Gradle project
This folder contains a Gradle project that can be compiled and run with a command-line interface or any other IDE that supports Gradle. An JDK of version 11 or 
higher is needed on your machine. You don't need to have Gradle installed on your machine. This is done automatically by the Gradle-wrapper

### IDEs
- The Gradle project can be opened with any IDE that supports Gradle projects. For example the Netbeans IDE
### Command-line interface
The Gradle project can also be compiled and run using a command-line interface. In your command-line interface navigate into the “Gradle Project for Netbeans” 
folder. Here are some commands you can then execute in the command-line interface:
- To compile the MSC 
```
gradlew compileJava
```
- To run the MSC
```
gradlew run
```
- To test the MSC
```
gradlew test
```
- To create a JAR file of the MSC
```
gradlew jar
```
- To create an executable "fat" JAR file for the MSC
```
gradlew fatJar
```
- To create the MSC javadoc
```
gradlew javadoc
```
- To delete the project build directory
```
gradlew clean
```
