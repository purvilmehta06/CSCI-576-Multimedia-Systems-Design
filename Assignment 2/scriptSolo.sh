# compile java file 
javac HW2.java

java HW2 "$@"

# remove class file
find . -name '*.class' -exec rm -f {} \;