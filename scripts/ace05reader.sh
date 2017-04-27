#mvn clean
#mvn dependency:copy-dependencies
mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse"
#MAINCLASS="CommandLineInterface"
PACKAGE="ACE2005"
MAINCLASS="Ace2005Reader"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
