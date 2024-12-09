#!/bin/bash

#
# Run compile_results.py over a folder of folders. The target folder should just container folders, which contain JSON files.
# The purpose of this script is to compile the results of multiple experiments into one R workspace.
#
# This script will generate a import.r script that contains a function to import all of the CSV files
# generated by compile_results.py. Once the script is generated it will run it, creating a RData file
# and will then open that file in RStudio for further work.
#
# Note: May only work in MacOS. At the very least it will not automatically open RStudio in Linux.
#

# usage: ./r_importer.sh {PATH TO FOLDER OF EXPERIMENT RESULTS}
root=$1

# Generate R script file
script=${root}/import.r
rm ${script}
touch ${script}

# Apply first few script lines, set the working directory, remove all current files
# and begin creating the function.
echo "setwd(\"${root}\")" >> ${script}
echo "rm(list = ls())" >> ${script}
echo "import <- function() {" >> ${script}

# Loop through all folders in a directory.
for dir in $root/*/
do
    dir=${dir%*/} 
    rename=$(basename $dir)

    echo "--- Processing $rename ---"

    # Run compile_results.py on folder.
    ./compile_results.py ${dir} ./experiments/basicExperiment.json
    killAll Numbers

    #Rename and properly format CSV file.
    source=${dir}/compiled-results-basicExperiment.csv
    target=${dir}/${rename}.csv
    mv $source $target

    # Remove first 4 lines and remove last line for proper csv formatting.
    tail -n +5 $target > ${target}.tmp && mv ${target}.tmp $target
    sed -i '' -e '$ d' ${target}

    # Append read statement to R script for CSV file.
    dataFrame=$(echo "$rename" | tr - _)
    echo "  $dataFrame <<- read.csv(\"$target\", header = TRUE)" >> ${script}
done  

# Finalize R stript. 
echo "}" >> ${script}
echo "import()" >> ${script}
echo "save.image(\"${root}data.RData\")" >> ${script}

# Run script and open generated RData file.
echo "--- Running R Import Script ---"
Rscript ${script}

echo "--- Opening RStudio ---"
open ${root}data.RData