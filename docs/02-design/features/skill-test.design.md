# Design: skill-test

## Architecture: Option C — Pragmatic Balance

### FR-01: 글자수 카운터
- `OutlinedTextField` 바로 아래 `Row`에 `Text("${content.length}자")` 추가
- `Modifier.align(Alignment.End)` 오른쪽 정렬
- `MaterialTheme.typography.labelSmall` + `onSurfaceVariant` 색상
- 별도 상태 불필요 — `content` 변수를 그대로 참조
