#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 6 ]; then
    echo "Usage: $0 <imagePath> <x> <y> <width> <height> <outputPath>"
    exit 1
fi

# Assign arguments to variables
imagePath=$1
x=$2
y=$3
width=$4
height=$5
outputPath=$6

# Define the source path
SOURCE_PATH="../src/main/java"

# Check if the source path exists
if [ ! -d "$SOURCE_PATH" ]; then
    echo "Error: Source path '$SOURCE_PATH' does not exist."
    exit 1
fi

# Compile the Java files
javac -d out -sourcepath $SOURCE_PATH $SOURCE_PATH/lambda/*.java

# Check if the compilation was successful
if [ $? -ne 0 ]; then
    echo "Error: Compilation failed."
    exit 1
fi

# Run the CropMain class
java -cp out main.java.lambda.CropMain "$imagePath" "$x" "$y" "$width" "$height" "$outputPath"