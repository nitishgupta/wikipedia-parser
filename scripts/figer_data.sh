mvn compile

DEFAULT_PACKAGE="edu.washington.cs.figer"
PACKAGE="data"
MAINCLASS="FigerData"
CP="./:./target/classes/:./target/dependency/*:./config/:target/dependency/*"
java -Xmx30g -cp $CP $DEFAULT_PACKAGE.$PACKAGE.$MAINCLASS                                                         
