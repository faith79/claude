# Design: tab-height-3x

## Context Anchor

| 항목 | 내용 |
|------|------|
| WHY | 64dp 탭이 너무 작아 터치 불편 |
| SUCCESS | height=192dp 적용, 빌드 성공 |
| SCOPE | HomeScreen.kt 1줄 수정 |

## Change Spec

| 파일 | 변경 전 | 변경 후 |
|------|---------|---------|
| `ui/home/HomeScreen.kt` | `NavigationBar(modifier = Modifier.height(64.dp))` | `NavigationBar(modifier = Modifier.height(192.dp))` |
