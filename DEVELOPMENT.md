# Build & Development

## Requirements
- Gradle
- Java SDK, version 11 or later

## Build the plugin
The build can be started with
```
    ./gradlew clean build
```
Use the corresponding command `.\gradlew.bat clean build` on Windows.

This command downloads the gradle distribution and then build the plugin
after it fetched the required dependencies.
After a successful build the plugin file is here (for a plugin with version 1.0.0):
```
    ./build/distributions/zato-1.0.0.zip
```

## Test the plugin
The file `zato-1.0.x.zip` can be tested in the different versions of PyCharm and IntelliJ
by choosing `File > Settings > Plugins > Install plugin from disk...`.
Choose the file in the dialog and restart the IDE after applying the settings. The plugin
will be active after the restart of your IDE.

## Upload the plugin
Official instructions how to deploy a plugin: https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html

### Steps to publish / update
- Update the plugin's version, if necessary. You can do this by modifying the value for `version = '...'` in the file `gradle.properties`.
- Run the gradle build: `./gradlew clean build runPluginVerifier`.
- If there were no errors during the build, then the file to upload is `./build/distributions/zato-$version.zip`
- Login at https://plugins.jetbrains.com and click on *Upload plugin* if you'd like to upload it for the first time. If you'd like to update an existing plugin, then open the plugin's page and click on *Update Plugin*.
- Select the file listed above in the upload control, add change notes if necessary. The plugin's category is probably *Networking*, but choose any other category at your preference. Also, fill out the other fields, like `License` etc.
- After this JetBrains will approve the new plugin. This might take a few hours.
- After its approval then plugin will be listed at https://plugins.jetbrains.com. You'll be able to see download statistics etc. after you logged in and open the plugins page.

## Development
This is only necessary if you'd like to work on the plugin's source code.
It's not required to build it.

### Setup IntelliJ
- Download the current version of IntelliJ IDEA Community or Ultimate.
    - List of older releases: https://www.jetbrains.com/idea/download/previous.html
- Open the project directory as a project, it's using the `build.gradle.kts` file 
  to synchronize the IntelliJ SDK and the Python plugin 

### Directory structure

- `src/main/java`: Java sources
- `src/main/resources`: Java resource files
- `src/test/java`: Java test case sources
- `src/test/resources`: Java test case resource files

