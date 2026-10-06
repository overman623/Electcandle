# Electcandle

Electcandle (수련초) is a candle app for breathing practice. When the wick is lit, the flame grows and shrinks with the breathing cycle, and tilts along with the device.

## Features

- Candle and flame on a portrait screen, and smoke when the session ends
- Flame tilt from the gyroscope
- Looping background audio, either a track bundled with the app or music on the device
- A bell at the inhale point and at the end of each breathing cycle
- When the session time is up, the bell rings and the background audio stops

The default breathing time is 60 seconds, and the default session time is 1 hour.

## Build

Open this folder in Android Studio. JDK 17 is required.

| Item | Value |
| --- | --- |
| Package | `com.happyhouse.electcandle` |
| Language | Java |
| compileSdk | 36 |
| minSdk | 23 |
| targetSdk | 28 |

`local.properties` is not in the repository. Android Studio writes the SDK path when it creates this file.

To sign a release build, add only the following to that same file locally. Do not commit the keystore or passwords.

```
release.storeFile=C\:\\path\\to\\appkey.jks
release.storePassword=
release.keyAlias=
release.keyPassword=
```

If these values are missing, debug builds are signed with Android Studio's debug key.

Ad units use Google test IDs. To switch to production IDs, replace `ad_app_id`, `ad_front`, `ad_end`, and `ad_banner` in `app/src/main/res/values/strings.xml`.

## Screens

- `MainActivity` — candle, timer, tilt, and ads
- `FlameAnimationView` — flame and smoke
- `MusicService` — background audio
- `SoundManager` — bell (`res/raw/ring`)
