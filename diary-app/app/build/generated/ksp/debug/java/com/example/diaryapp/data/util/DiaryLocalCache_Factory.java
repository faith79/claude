package com.example.diaryapp.data.util;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DiaryLocalCache_Factory implements Factory<DiaryLocalCache> {
  private final Provider<Context> contextProvider;

  private DiaryLocalCache_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DiaryLocalCache get() {
    return newInstance(contextProvider.get());
  }

  public static DiaryLocalCache_Factory create(Provider<Context> contextProvider) {
    return new DiaryLocalCache_Factory(contextProvider);
  }

  public static DiaryLocalCache newInstance(Context context) {
    return new DiaryLocalCache(context);
  }
}
