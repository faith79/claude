package com.example.diaryapp.viewmodel;

import androidx.work.WorkManager;
import com.example.diaryapp.notification.NotificationPreferences;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<NotificationPreferences> notificationPreferencesProvider;

  private final Provider<WorkManager> workManagerProvider;

  private SettingsViewModel_Factory(
      Provider<NotificationPreferences> notificationPreferencesProvider,
      Provider<WorkManager> workManagerProvider) {
    this.notificationPreferencesProvider = notificationPreferencesProvider;
    this.workManagerProvider = workManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(notificationPreferencesProvider.get(), workManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<NotificationPreferences> notificationPreferencesProvider,
      Provider<WorkManager> workManagerProvider) {
    return new SettingsViewModel_Factory(notificationPreferencesProvider, workManagerProvider);
  }

  public static SettingsViewModel newInstance(NotificationPreferences notificationPreferences,
      WorkManager workManager) {
    return new SettingsViewModel(notificationPreferences, workManager);
  }
}
