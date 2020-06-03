#!/bin/sh
chmod -R 777 .
./jdk-11.0.2/Contents/Home/bin/java -Xmx8g -jar ./Libraries/MoleculeSetComparator-fat-1.0.jar
exit
