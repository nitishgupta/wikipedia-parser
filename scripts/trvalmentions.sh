#mvn clean
#mvn dependency:copy-dependencies
mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse"
PACKAGE="wikiextractparser"
MAINCLASS="TrainTestMentions"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
time java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
