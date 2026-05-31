package com.example.diaryapp;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class DiaryApp_MembersInjector implements MembersInjector<DiaryApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private DiaryApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  @Override
  public void injectMembers(DiaryApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  public static MembersInjector<DiaryApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new DiaryApp_MembersInjector(workerFactoryProvider);
  }

  @InjectedFieldSignature("com.example.diaryapp.DiaryApp.workerFactory")
  public static void injectWorkerFactory(DiaryApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
