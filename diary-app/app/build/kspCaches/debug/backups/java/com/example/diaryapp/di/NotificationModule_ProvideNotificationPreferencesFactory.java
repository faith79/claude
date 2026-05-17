package com.example.diaryapp.di;

import android.content.Context;
import com.example.diaryapp.notification.NotificationPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NotificationModule_ProvideNotificationPreferencesFactory implements Factory<NotificationPreferences> {
  private final Provider<Context> contextProvider;

  private NotificationModule_ProvideNotificationPreferencesFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotificationPreferences get() {
    return provideNotificationPreferences(contextProvider.get());
  }

  public static NotificationModule_ProvideNotificationPreferencesFactory create(
      Provider<Context> contextProvider) {
    return new NotificationModule_ProvideNotificationPreferencesFactory(contextProvider);
  }

  public static NotificationPreferences provideNotificationPreferences(Context context) {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideNotificationPreferences(context));
  }
}
