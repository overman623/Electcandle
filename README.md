# 수련초 (Electcandle)

호흡 수련용 촛불 앱입니다. 심지를 켜면 불꽃이 호흡 주기에 맞춰 커졌다 작아지고, 기기를 기울이면 불꽃도 함께 기울어집니다.

## 기능

- 세로 화면의 촛불과 불꽃, 수련이 끝나면 연기
- 자이로스코프로 불꽃 기울기
- 배경음 반복 재생, 앱에 포함된 곡 또는 기기의 음악
- 호흡 한 사이클의 들숨 지점과 끝에서 종소리
- 수련 시간이 끝나면 종을 치고 배경음을 멈춤

기본 호흡 시간은 60초, 기본 수련 시간은 1시간입니다.

## 빌드

Android Studio에서 이 폴더를 엽니다. JDK 17이 필요합니다.

| 항목 | 값 |
| --- | --- |
| 패키지 | `com.happyhouse.electcandle` |
| 언어 | Java |
| compileSdk | 36 |
| minSdk | 23 |
| targetSdk | 28 |

`local.properties`는 저장소에 없습니다. SDK 경로는 Android Studio가 이 파일을 만들 때 넣습니다.

릴리스 서명이 필요하면 같은 파일에 아래만 로컬로 추가합니다. 키스토어와 비밀번호는 커밋하지 않습니다.

```
release.storeFile=C\:\\path\\to\\appkey.jks
release.storePassword=
release.keyAlias=
release.keyPassword=
```

이 값이 없으면 debug 빌드는 Android Studio의 debug 키로 서명됩니다.

광고 단위는 Google 테스트 ID입니다. 배포용 ID로 바꾸려면 `app/src/main/res/values/strings.xml`의 `ad_app_id`, `ad_front`, `ad_end`, `ad_banner`를 교체합니다.

## 화면

- `MainActivity` — 촛불, 타이머, 기울기, 광고
- `FlameAnimationView` — 불꽃과 연기
- `MusicService` — 배경음
- `SoundManager` — 종소리 (`res/raw/ring`)
