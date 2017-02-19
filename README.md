# super-performance-sprites
1000 sprites on a low/medium end mobile phone using one draw call in OpenGL ES

- To use as Maven project in Eclipse, import as Existing Maven project.

- To use as Gradle project in Eclipse (Neon) - import Super-performance-sprites as gradle project.
On Eclipse Neon there is no support for Android Gradle projects so you need to do the following:
- Delete the Super-performance-sprites-android sub-project (do NOT delete contents from disk)
- Import Android code from 'super-performance-sprites-android' and make sure Java compiler is set to 1.7
- Open 'properties-java build path', delete 'super-performance-sprites-android/src'
- Click 'Add folder' and select the subfolder 'src/main/java'
- Reference graphics-by-opengl-android and super-performance-sprites-j2se

ADT needs to be installed.

pom file may display error similar to:

Plugin execution not covered by lifecycle configuration: com.simpligility.maven.plugins:android-maven-plugin:4.4.1:emma (execution: default-emma, phase: process-classes)

- To resolve this, choose quick fix 'Permanently mark goal emma in pom.xml as ignored in Eclipse build'

You may experience problem with non-existing project.properties file 

- I solved by adding an empty project.properties file in the Android project root, chosing 'Properties-Android' and selecting a valid SDK.

Maven - update project,or clean build to get rid of any trailing errors.

Gradle - make sure you have run 'gradle publishToMavenLocal' for dependent libraries 'vecmath', 'graphics-engine' and 'graphics-by-opengl'






