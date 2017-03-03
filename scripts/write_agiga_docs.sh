#mvn clean
#mvn dependency:copy-dependencies
mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse"
#PACKAGE="be.tarsos.lsh"
#MAINCLASS="CommandLineInterface"
PACKAGE="agiga"
MAINCLASS="AgigaDocuments"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
java -Xmx60g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
