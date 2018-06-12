# super-performance-sprites
2000 sprites on a low-end mobile phone using one draw call in OpenGL ES - this is a multi platform project that supports running on J2SE using JOGAMP.
- Demo with 5000 sprites, pinch to zoom and touch to release sprites.
Download and install 'super-performance-sprites-android.apk'


This project depends on:
vecmath (develop)
graphics-by-opengl (develop)
graphics-engine (develop)

Code style and formatting:
Follow the Google Java guidelines:
https://google.github.io/styleguide/javaguide.html

Eclipse:
Use customformatter.xml
Open preferences-general-workspace

Make sure Text file encoding is UTF-8
New text file delimiter - Unix


----------------------------------------------------------------------
ECLIPSE 
----------------------------------------------------------------------
Prerequisites:
- Maven
- Eclipse Oxygen
Make sure that you are using Android Andmore and m2e plugins (not the old DDMS/ADT from 'The Android Open Source Project')
Check by opening 'Help' - 'Install new software' - 'What is already installed?' 
Uninstall software from 'The Android Opensource Project' and fetch Andmore from Eclipse marketplace.

- JDK 1.7 or 1.8 (Not 1.9)
Check with 'javac -version' 

- Import dependencies as Maven projects or install to local maven repo:
- Import as Existing Maven project into Eclipse

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

-----------------------------------------------------------------------
Android Studio / gradle
-----------------------------------------------------------------------
Prerequisites:
- Gradle
- Android Studio 3.0
- JDK 1.7 or 1.8 (Not 1.9)
Check with 'javac -version' 

I do not have a full solution for how to include in Android Studio, dependend projects needs to be installed as local maven.
To build, first publish dependent projects to maven local by executing task 'publishToMavenLocal' for each module.

vecmath>gradle publishToMavenLocal
>graphics-by-opengl/graphics-by-opengl-j2se>gradle publishToMavenLocal
>graphics-by-opengl/graphics-by-opengl-android>gradle publishToMavenLocal
>graphics-engine>gradle publishToMavenLocal
>super-performance-sprites/super-performance-sprites-j2se>gradle publishToMavenLocal
>super-performance-sprites/super-performance-sprites-android>gradle assemble

Import as existing Android Studio / gradle project.

----------------------------------------------------------------------------









