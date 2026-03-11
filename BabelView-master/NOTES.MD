### Anne's Notes ###

Install the Android SDK from here:
https://developer.android.com/studio

The BabelView-raid folders contain the exact same code as master branch, as far as I can see.

My android.jar is placed at:
C:\Program Files\Android\Android Studio\plugins\android\lib\android.jar

Their README says to use the extract_jar.sh script to "extract the required files from android.jar." but this script doesn't exist anywhere! Instead trying:
Navigate to android-31 folder. Then (in Ubuntu terminal):
jar xf "/mnt/c/program files/Android/Android Studio/plugins/android/lib/android.jar"
(need to install 'jar' for this. apt install default.jdk)

**Don't think this is needed actually! Just need the android.jar in that folder!?**

Make sure BabelView.jar is in same folder as SourcesAndSinks.txt, AndroidCallbacks.txt, and EasyTaintWrapperSource.txt.

The command to run BabelView is strange. I don't know what the 'saveflows' is. For now, I've just created my own folder called 'saveflow', and will use that as argument.
According to ArgParser.java: "save the flow analysis results in a XML file".

Run BabelView:
java -jar BabelView.jar -apk myApks/gridle_122.apk -jars Android-Platforms -saveflows /saveflow -chain

## Problems & Notes ##
When running the tool for Gridle and Duckduckgo, it throws the 'NoJavaScriptInterfacesException: This app does not have any method annotated with @JavascriptInterface".
I cannot determine if this is because the tool is old, and that this might not be standard practice anymore, or that this means that the apps don't have an EXPOSED JavaScript interface.

## Apks ##
https://f-droid.org/en/packages/com.duckduckgo.mobile.android/
