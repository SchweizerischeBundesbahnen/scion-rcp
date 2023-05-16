#!/bin/bash

exit_if_failed () {
  if [[ $1 -ne 0 ]] ; then 
    echo $2 ; exit 1
  fi
}

exit_if_empty () {
  if [[ -z $1 ]] ; then 
    echo $2 ; exit 1
  fi
}

exit_if_empty "$RELEASE_VERSION" "RELEASE_VERSION must be set"
release_version=$RELEASE_VERSION
echo "Release: $release_version"

exit_if_empty "$DEVELOP_VERSION" "Next development iteration: $develop_version"
develop_version=$DEVELOP_VERSION
echo "Next development iteration: $develop_version"

exit_if_empty "$STAGING_PROFILE_ID" "STAGING_PROFILE_ID must be set"
staging_profile_id=$STAGING_PROFILE_ID
echo "Staging profile ID: $staging_profile_id"

work_dir=`pwd`
echo "Working directory: $work_dir"

i=1
echo
echo "Step $i: Ensure git author identity" ; ((++i))
# Note: The release:prepare step already requires a git author identity to be present, therefore, check it beforehand.
if [[ -z `git config --get user.name` || -z `git config --get user.email` ]] ; then
  echo "No git user.name or user.email found!"
  echo "Setting git user.name to github-actions and user.email to github-actions@github.com"
  git config user.name github-actions
  git config user.email github-actions@github.com  
fi
echo "git user.name: `git config --get user.name`"
echo "git user.email: `git config --get user.email`"
 
echo
echo "Step $i: Prepare release" ; ((++i))
mvn -B release:clean release:prepare \
  -DscmDevelopmentCommitComment="develop(scion-rcp): prepare for next development iteration v$develop_version" \
  -DscmReleaseCommitComment="release(scion-rcp): v$release_version" \
  -DreleaseVersion="$release_version" \
  -Dtag="$release_version" \
  -DdevelopmentVersion="$develop_version" \
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
exit_if_failed "$?" "Prepare release failed, exiting"

echo
echo "Step $i: Push commits and release tag" ; ((++i))
# Delete potentially existing release tag:
git push origin :refs/tags/"$release_version"
# Push commits:
git push
# Push new release tag:
git push origin tag "$release_version"

echo
echo "Step $i: Create local stage" ; ((++i))
local_stage="$work_dir/target/local-stage/$staging_profile_id"
echo "Creating $local_stage"
mkdir -p $local_stage

echo
echo "Step $i: Stage artifacts locally ($local_stage)" ; ((++i))
mvn -B release:perform \
  -Dgoals="deploy -DaltDeploymentRepository=local::file:///$local_stage" \
  -DconnectionUrl="https://github.com/SchweizerischeBundesbahnen/scion-rcp.git" \
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
exit_if_failed "$?" "Stage artifacts locally failed, exiting"

echo
echo "Step $i: Upload staged artifacts to remote stage (Nexus repository)" ; ((++i))
# Go to local stage to be in a directory that does not contain a pom. Otherwise maven would use the pom for executing the command which we do not want.
echo "Changing to $local_stage"
cd $local_stage
mvn -B org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13:deploy-staged-repository \
  -DnexusUrl="https://oss.sonatype.org" \
  -DserverId="ossrh" \
  -DrepositoryDirectory=. \
  -DstagingProfileId="$staging_profile_id"
echo "Returning to $work_dir"
cd $work_dir

