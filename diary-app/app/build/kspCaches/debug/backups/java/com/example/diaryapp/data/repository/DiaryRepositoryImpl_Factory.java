package com.example.diaryapp.data.repository;

import com.example.diaryapp.data.source.FirestoreDataSource;
import com.example.diaryapp.data.source.StorageDataSource;
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
public final class DiaryRepositoryImpl_Factory implements Factory<DiaryRepositoryImpl> {
  private final Provider<FirestoreDataSource> firestoreDataSourceProvider;

  private final Provider<StorageDataSource> storageDataSourceProvider;

  private DiaryRepositoryImpl_Factory(Provider<FirestoreDataSource> firestoreDataSourceProvider,
      Provider<StorageDataSource> storageDataSourceProvider) {
    this.firestoreDataSourceProvider = firestoreDataSourceProvider;
    this.storageDataSourceProvider = storageDataSourceProvider;
  }

  @Override
  public DiaryRepositoryImpl get() {
    return newInstance(firestoreDataSourceProvider.get(), storageDataSourceProvider.get());
  }

  public static DiaryRepositoryImpl_Factory create(
      Provider<FirestoreDataSource> firestoreDataSourceProvider,
      Provider<StorageDataSource> storageDataSourceProvider) {
    return new DiaryRepositoryImpl_Factory(firestoreDataSourceProvider, storageDataSourceProvider);
  }

  public static DiaryRepositoryImpl newInstance(FirestoreDataSource firestoreDataSource,
      StorageDataSource storageDataSource) {
    return new DiaryRepositoryImpl(firestoreDataSource, storageDataSource);
  }
}
