#mvn clean
#mvn dependency:copy-dependencies
mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse"
#PACKAGE="be.tarsos.lsh"
#MAINCLASS="CommandLineInterface"
PACKAGE="nyt"
MAINCLASS="NYTProcessor"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
