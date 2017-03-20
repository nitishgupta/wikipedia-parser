#mvn clean
#mvn dependency:copy-dependencies
mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse"
#PACKAGE="be.tarsos.lsh"
#MAINCLASS="CommandLineInterface"
PACKAGE="wikiextractparser"
MAINCLASS="TestDataKnownEntitiesOverlap"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
time java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
