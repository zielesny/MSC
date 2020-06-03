#!/bin/sh
chmod -R a+rwx .
./jdk-11.0.2/bin/java -Xmx4g -jar ./lib/MoleculeSetComparator-fat-1.0.jar
exit
