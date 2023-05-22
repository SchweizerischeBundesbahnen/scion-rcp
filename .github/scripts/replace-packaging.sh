#!/bin/bash

pom_file="$1"

if [[ -f $pom_file ]] ; then
  echo "Attempting to replace eclipse-packaging with jar packaging in $pom_file"
  sed -i 's#<packaging>eclipse-plugin</packaging>#<packaging>jar</packaging>#' "$pom_file"
fi