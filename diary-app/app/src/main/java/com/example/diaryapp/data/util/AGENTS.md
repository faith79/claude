<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# util

## Purpose
Local utilities for the data layer — in-memory diary cache and image compression before upload.

## Key Files

| File | Description |
|------|-------------|
| `DiaryLocalCache.kt` | In-memory cache of diary entries keyed by date; invalidated after writes |
| `ImageCompressor.kt` | Compresses `Uri` images to ≤ 1 MB JPEG before uploading to Firebase Storage |

## For AI Agents

### Working In This Directory
- `DiaryLocalCache` prevents redundant Firestore reads for already-fetched entries
- Cache must be invalidated in `DiaryViewModel` after any create/update/delete
- `ImageCompressor` should be called before `StorageDataSource.uploadImage()` — never upload raw camera images
- Max 3 images per diary entry — enforce in ViewModel before calling compressor

### Common Patterns
- Cache is a `HashMap<String, DiaryEntry>` where key is `"yyyy-MM-dd"` date string
- Compression target: 1024×1024 max resolution, 80% JPEG quality

<!-- MANUAL: -->
