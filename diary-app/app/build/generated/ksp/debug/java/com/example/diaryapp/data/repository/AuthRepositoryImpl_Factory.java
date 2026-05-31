package com.example.diaryapp.data.repository;

import com.example.diaryapp.data.source.AuthDataSource;
import com.example.diaryapp.security.CredentialStorage;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthDataSource> authDataSourceProvider;

  private final Provider<CredentialStorage> credentialStorageProvider;

  private AuthRepositoryImpl_Factory(Provider<AuthDataSource> authDataSourceProvider,
      Provider<CredentialStorage> credentialStorageProvider) {
    this.authDataSourceProvider = authDataSourceProvider;
    this.credentialStorageProvider = credentialStorageProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authDataSourceProvider.get(), credentialStorageProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthDataSource> authDataSourceProvider,
      Provider<CredentialStorage> credentialStorageProvider) {
    return new AuthRepositoryImpl_Factory(authDataSourceProvider, credentialStorageProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthDataSource authDataSource,
      CredentialStorage credentialStorage) {
    return new AuthRepositoryImpl(authDataSource, credentialStorage);
  }
}
