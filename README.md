# super-performance-sprites
1000 sprites on a low/medium end mobile phone using one draw call in OpenGL ES, point on the screen to move sprites.

To use the project in Eclipse, import as Existing Maven project.

ADT needs to be installed.

pom file may display error similar to:

Plugin execution not covered by lifecycle configuration: com.simpligility.maven.plugins:android-maven-plugin:4.4.1:emma (execution: default-emma, phase: process-classes)

- To resolve this, choose quick fix 'Permanently mark goal emma in pom.xml as ignored in Eclipse build'

You may experience problem with non-existing project.properties file 

- I solved by adding an empty project.properties file in the Android project root, chosing 'Properties-Android' and selecting a valid SDK.

Maven - update project,or clean build to get rid of any trailing errors.




