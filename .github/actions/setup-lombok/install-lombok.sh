#!/bin/bash

if [[ $# -lt 1 ]] ; then
  echo "Usage: ./install-lombok <lombok-version> <optional: install-dir>" ; exit 1
fi

lombok_version=$1
install_dir=$2
if [[ -z $install_dir ]] ; then
  install_dir="/opt/lombok"
  mkdir -p $install_dir
fi
if [[ ! -d $install_dir ]] ; then
  echo "Installation directory does not exist, or could not be created ($install_dir), exiting" ; exit 1
fi

install_lombok () {
  mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.5.0:copy \
   -Dartifact=org.projectlombok:lombok:"$lombok_version":jar \
   -DoutputDirectory="$install_dir" \
   -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
}

work_dir=$(pwd)
# Change to installation directory to avoid using any POM that may reside in the source directory
cd "$install_dir" || exit 1
if ! install_lombok ; then
  echo "Failed to set up Lombok, exiting" ; exit 1
fi
# Return to source directory
cd "$work_dir" || exit 1

# Write jar location into output
echo "lombok-jar=$install_dir/lombok-$lombok_version.jar" >> "$GITHUB_OUTPUT"


