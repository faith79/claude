package com.example.diaryapp.viewmodel;

import com.example.diaryapp.data.repository.DiaryRepository;
import com.example.diaryapp.data.util.ImageCompressor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DiaryViewModel_Factory implements Factory<DiaryViewModel> {
  private final Provider<DiaryRepository> diaryRepositoryProvider;

  private final Provider<ImageCompressor> imageCompressorProvider;

  private DiaryViewModel_Factory(Provider<DiaryRepository> diaryRepositoryProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    this.diaryRepositoryProvider = diaryRepositoryProvider;
    this.imageCompressorProvider = imageCompressorProvider;
  }

  @Override
  public DiaryViewModel get() {
    return newInstance(diaryRepositoryProvider.get(), imageCompressorProvider.get());
  }

  public static DiaryViewModel_Factory create(Provider<DiaryRepository> diaryRepositoryProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    return new DiaryViewModel_Factory(diaryRepositoryProvider, imageCompressorProvider);
  }

  public static DiaryViewModel newInstance(DiaryRepository diaryRepository,
      ImageCompressor imageCompressor) {
    return new DiaryViewModel(diaryRepository, imageCompressor);
  }
}
