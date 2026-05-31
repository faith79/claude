package com.example.diaryapp.security;

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
public final class CredentialStorage_Factory implements Factory<CredentialStorage> {
  private final Provider<Context> contextProvider;

  private CredentialStorage_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CredentialStorage get() {
    return newInstance(contextProvider.get());
  }

  public static CredentialStorage_Factory create(Provider<Context> contextProvider) {
    return new CredentialStorage_Factory(contextProvider);
  }

  public static CredentialStorage newInstance(Context context) {
    return new CredentialStorage(context);
  }
}
