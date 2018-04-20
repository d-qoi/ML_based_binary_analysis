#! /bin/bash

if [[ $# -ne 2 ]]
then
    echo "Please pass directory to write to and a number to generate"
    echo "generate_data.sh ./data 100"
    exit 1
fi

#while [[ $# -gt 0 ]]
#do
#    echo $1
#    shift #
#done

for i in `seq 1 $2`
do
    echo $i
    bin/Generator $1/dump_$i.csv
done
