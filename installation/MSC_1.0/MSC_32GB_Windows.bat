if not DEFINED IS_MINIMIZED set IS_MINIMIZED=1 && start "" /min "%~dpnx0" %* && exit
@echo off
.\jdk-11.0.2\bin\java -Xmx32g -jar .\lib\MoleculeSetComparator-fat-1.0.jar
exit
