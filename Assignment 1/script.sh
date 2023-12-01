# compile java file 
javac HW1.java

# run java file
java HW1 $1 $2 $3 $4

# remove class file
find . -name '*.class' -exec rm -f {} \;