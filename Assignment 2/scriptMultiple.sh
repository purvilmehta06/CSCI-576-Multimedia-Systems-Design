# compile java file 
javac HW2.java

java HW2 "./multi_object_test/Kirby_Warning.rgb" "./dataset/Kirby_object.rgb" "./dataset/Warning_object.rgb"  > "logs/Kirby_Warning.txt"
java HW2 "./multi_object_test/Multiple_Volleyballs_v2.rgb" "./dataset/Oswald_object.rgb" "./dataset/Volleyball_object.rgb"  > "logs/Multiple_Volleyballs_v2.txt"
java HW2 "./multi_object_test/Oswald_and_Volleyball_v2.rgb" "./dataset/Oswald_object.rgb" "./dataset/Volleyball_object.rgb"  > "logs/Oswald_and_Volleyball_v2.txt"
java HW2 "./multi_object_test/Oswald_and_Volleyball.rgb" "./dataset/Oswald_object.rgb" "./dataset/Volleyball_object.rgb"  > "logs/Oswald_and_Volleyball.txt"
java HW2 "./multi_object_test/Pikachu_and_Oswald_v2.rgb" "./dataset/Pikachu_object.rgb" "./dataset/Oswald_object.rgb"  > "logs/Pikachu_and_Oswald_v2.txt"

# remove class file
find . -name '*.class' -exec rm -f {} \;