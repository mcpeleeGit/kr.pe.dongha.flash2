# 간편 플래시

원터치로 빠르게 켜고 끄는 심플한 Android 플래시 앱입니다.

## 주요 기능

* 원터치 플래시 ON/OFF
* 화면 조명 모드
* SOS 깜빡임
* 자동 꺼짐 타이머
* 플래시 미지원 기기 대응
* 앱 종료 시 자동 OFF

## 기술 스택

* Kotlin
* Jetpack Compose
* Camera2 API
* CameraManager.setTorchMode

## GitHub Description

간편 플래시 - 원터치로 빠르게 켜고 끄는 심플한 Android 플래시 앱

## 실행 방법

1. Android Studio에서 프로젝트를 엽니다.
2. Gradle Sync를 실행합니다.
3. Android 6.0(API 23) 이상 기기 또는 에뮬레이터를 선택합니다.
4. Run 버튼으로 앱을 실행합니다.

실제 플래시 동작은 후면 플래시가 있는 실기기에서 확인하는 것을 권장합니다.

## 릴리스 빌드

```bash
./gradlew :app:bundleRelease
```

Play Console에 업로드할 App Bundle은 `app/build/outputs/bundle/release/app-release.aab`에 생성됩니다.
R8 가독화 파일은 `app/build/outputs/mapping/release/mapping.txt`에 생성되며, Play Console의 ReTrace 매핑 파일로 함께 업로드합니다.
Compose가 끌고 오는 선택적 AndroidX path 네이티브 라이브러리는 앱 기능에 사용하지 않아 릴리스 번들에서 제외합니다.
