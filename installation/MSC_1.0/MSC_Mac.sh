#!/bin/sh
chmod -R 777 .
./jdk-11.0.2/bin/java -Xmx4g -jar ./Libraries/MoleculeSetComparator-fat-1.0.jar
exit
