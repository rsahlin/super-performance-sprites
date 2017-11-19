# super-performance-sprites
1000 sprites on a low/medium end mobile phone using one draw call in OpenGL ES - this is a multi platform project that supports running on J2SE using JOGAMP.
Tested with Eclipse Android Neon, since this is a multi platform projec - import as Maven project.
Make sure that you are using Android Andmore and m2e plugins (not the old DDMS/ADT from 'The Android Open Source Project')
Check by opening 'Help' - 'Install new software' - 'What is already installed?' 
Uninstall software from 'The Android Opensource Project' and fetch Andmore from Eclipse marketplace.


- To use as Maven project in Eclipse, import as Existing Maven project.
- Import dependencies as Maven projects or install to local maven repo:
This project depends on:
vecmath
graphics-by-opengl
graphics-engine

- Android modules may complain that compiler level is below 1.7 - to fix open 'properties' - 'java compiler' and make sure project uses compiler level (at least) 1.7
- Error similar to: Dex Loader] Failed to load .....\build-tools\27.0.1\lib\dx.jar although the jar is present
is solved by switching Android sdk build tools - uninstall the build tools version that is complaining (27.0.1) and install 25.0.3
Restart Eclipse to make sure changes take effect.
- If you see an error launching Android project:
Errors running builder 'Android Package Builder'
sun/misc/BASE64Encode
It probably means you are using Java 9 jre/sdk.
Download Java 8 then go to 'preferences' - 'Java' - 'Compiler' press 'Configure' att the bottom and add the downloaded java 8 jre/jdk and select as default.
Restart Eclipse to make sure changes take effect. 

Using gradle:
I do not have a full solution for how to include in Android Studio.
To build, first publish dependent projects to maven local by executing task 'publishToMavenLocal' for each module.

vecmath>gradle publishToMavenLocal
>graphics-by-opengl/graphics-by-opengl-j2se>gradle publishToMavenLocal
>graphics-by-opengl/graphics-by-opengl-android>gradle publishToMavenLocal
>graphics-engine>gradle publishToMavenLocal
>super-performance-sprites/super-performance-sprites-j2se>gradle publishToMavenLocal
>super-performance-sprites/super-performance-sprites-android>gradle assemble



----------------------------------------------------------------------------
Old instructions for Eclipse NEON - not valid anymore 
- To use as Gradle project in Eclipse (Neon) - import Super-performance-sprites as gradle project.
On Eclipse Neon there is no support for Android Gradle projects so you need to do the following:
- Delete the Super-performance-sprites-android sub-project (do NOT delete contents from disk)
- Import Android code from 'super-performance-sprites-android' and make sure Java compiler is set to 1.7
- Open 'properties-java build path', delete 'super-performance-sprites-android/src'
- Click 'Add folder' and select the subfolder 'src/main/java'
- Reference graphics-by-opengl-android and super-performance-sprites-j2se
- When running Android app if you see this error 'Dx unsupported class file version 52.0' it means that the referenced class is compiled with a Java compiler > 1.7 and Android is not compatible with that. 
To resolve make sure referenced libraries vecmath, graphics-engine, graphics-by-opengl are built using compiler version 1.7. This should not happen if libraries are built using gradle, but if libraries are also imported to Eclipse the default compiler level may differ.


ADT needs to be installed.

pom file may display error similar to:

Plugin execution not covered by lifecycle configuration: com.simpligility.maven.plugins:android-maven-plugin:4.4.1:emma (execution: default-emma, phase: process-classes)

- To resolve this, choose quick fix 'Permanently mark goal emma in pom.xml as ignored in Eclipse build'

You may experience problem with non-existing project.properties file 

- I solved by adding an empty project.properties file in the Android project root, chosing 'Properties-Android' and selecting a valid SDK.

Maven - update project, or clean build to get rid of any trailing errors.

Gradle - make sure you have run 'gradle publishToMavenLocal' for dependent libraries 'vecmath', 'graphics-engine' and 'graphics-by-opengl'










