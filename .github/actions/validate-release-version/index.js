const core = require('@actions/core');

try {
    // Validate input release version:
    const releaseVersionInput = core.getInput('release-version');
    const releaseVersionPattern = core.getInput('release-version-pattern');
    console.log(`Validating release version ${releaseVersionInput} against pattern ${releaseVersionPattern}`);
    const result = releaseVersionInput.match(new RegExp(`^${releaseVersionPattern}$`));
    if(result == null) {
        core.setFailed('Invalid release version, aborting release!');
    }

    // Assemble output release version:
    const releaseVersion = `${result.groups.major}.${result.groups.minor}.${result.groups.patch}`;
    core.setOutput("release-version", releaseVersion);
    console.log(`Release version is ${releaseVersion}`);

    // Assemble next development iteration version:
    const developVersion = `${result.groups.major}.${result.groups.minor}.${parseInt(result.groups.patch) + 1}-SNAPSHOT`;
    core.setOutput("develop-version", developVersion);
    console.log(`Next development iteration version is ${developVersion}`);
} catch (error) {
    core.setFailed(error.message);
}