WEARABLE_SERIAL="emulator-5554"
PHONE_SERIAL="emulator-5556"

ANDROID_SERIAL=$WEARABLE_SERIAL ./gradlew :wear:installDebug
adb -s $WEARABLE_SERIAL shell monkey -p com.ramitsuri.locationtracking.debug -c android.intent.category.LAUNCHER 1

ANDROID_SERIAL=$PHONE_SERIAL ./gradlew :phone:installDebug
adb -s $PHONE_SERIAL shell monkey -p com.ramitsuri.locationtracking.debug -c android.intent.category.LAUNCHER 1
adb -s $PHONE_SERIAL shell settings put system accelerometer_rotation 0
