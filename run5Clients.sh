#!/bin/bash

for i in {1..5}
do
    scala client/target/scala-2.12/paidy-restaurant-assembly-0.0.1-SNAPSHOT.jar $(jq -c . generated$i.json) &
done

wait
echo "All Done"
