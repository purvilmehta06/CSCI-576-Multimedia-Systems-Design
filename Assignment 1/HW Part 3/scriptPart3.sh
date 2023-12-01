# compile java file 
javac HW1Part3.java

# run java file
java HW1Part3

# remove class file
find . -name '*.class' -exec rm -f {} \;

# plot percetange noise vs error rate graph
python3 plot.py