package com.example.diaryapp.viewmodel;

import com.example.diaryapp.data.repository.DiaryRepository;
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

  private DiaryViewModel_Factory(Provider<DiaryRepository> diaryRepositoryProvider) {
    this.diaryRepositoryProvider = diaryRepositoryProvider;
  }

  @Override
  public DiaryViewModel get() {
    return newInstance(diaryRepositoryProvider.get());
  }

  public static DiaryViewModel_Factory create(Provider<DiaryRepository> diaryRepositoryProvider) {
    return new DiaryViewModel_Factory(diaryRepositoryProvider);
  }

  public static DiaryViewModel newInstance(DiaryRepository diaryRepository) {
    return new DiaryViewModel(diaryRepository);
  }
}
