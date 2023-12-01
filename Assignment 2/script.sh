# compile java file 
javac HW2.java

file_names=("Apple" "Kirby" "Oswald" "Pikachu" "rose" "strawberry" "USC" "Volleyball")
directory_path="./dataset"

for file_name in "${file_names[@]}"; do
  echo "Processing file: $file_name"
    java HW2 "$directory_path/$file_name"_image.rgb "$directory_path/$file_name"_object.rgb  > "logs/$file_name.txt"
done

# remove class file
find . -name '*.class' -exec rm -f {} \;