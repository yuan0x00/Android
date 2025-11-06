#android
PACKAGE_NAME="com.rapid.android"
ACTIVITY_NAME=".feature.main.MainActivity"

#PACKAGE_NAME="com.rapid.android"
#ACTIVITY_NAME=".feature.splash.SplashActivity"

#compose
#PACKAGE_NAME="com.rapid.compose"
#ACTIVITY_NAME=".MainActivity"

#libchecker
#PACKAGE_NAME="com.absinthe.libchecker"
#ACTIVITY_NAME=".features.home.ui.MainActivity"

adb shell am force-stop "$PACKAGE_NAME" && adb shell am start -W "$PACKAGE_NAME"/"$ACTIVITY_NAME"