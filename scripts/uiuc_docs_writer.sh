mvn compile

DEFAULT_PACKAGE="edu.illinois.cs.cogcomp.wikiparse.datasets"
PACKAGE="uiuc"
MAINCLASS="UIUCDocWriter"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         