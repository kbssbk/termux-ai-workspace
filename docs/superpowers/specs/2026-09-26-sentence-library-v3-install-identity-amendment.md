# 문장 라이브러리 V3 — 설치 식별자/서명 보완

작성일: 2026-09-26
대상 스펙: `docs/superpowers/specs/2026-09-26-sentence-library-v3-design.md`

## 추가 요구

기존 `com.beomsoo.sentenceapp` 패키지 식별자를 다른 APK/앱에서도 사용한 이력이 있으므로 V3는 기존 앱과 충돌하지 않는 새 Android 애플리케이션 식별자를 사용한다.

## 확정값

- Android `applicationId`: `com.beomsoo.sentencelibrary`
- Kotlin/Android namespace: `com.beomsoo.sentencelibrary`
- 표시 이름: `문장 라이브러리`
- V3 최초 `versionName`: `3.0.0`
- V3 최초 `versionCode`: `30000`
- 기존 V2/다른 앱과 나란히 설치할 수 있어야 한다.
- 이후 V3 업데이트에서는 `applicationId`를 변경하지 않는다.

## 데이터 이전

패키지 샌드박스가 달라지므로 이전 앱의 WebView localStorage를 직접 읽는 방식에 의존하지 않는다. 기존 앱에서 내보낸 V2 JSON 백업 파일을 V3에서 가져와 Room 데이터베이스로 변환하는 경로를 공식 이전 경로로 제공한다.

## 서명 원칙

- 최종 배포 APK는 장기 업데이트를 위해 고정된 사용자 소유 서명키로 서명한다.
- 개인 서명키/비밀번호는 공개 GitHub 저장소에 커밋하지 않는다.
- 프로젝트에는 `keystore.properties.example`과 서명 스크립트만 저장한다.
- 최초 배포 시 새 V3 전용 keystore를 생성하고 사용자가 별도 보관할 수 있는 파일로 제공한다.
- 이후 같은 `applicationId` 앱을 업데이트하려면 동일 keystore가 필요하므로 keystore 분실 경고를 앱/README에 명시한다.

이 문서는 원 설계 스펙의 일부로 취급한다.
