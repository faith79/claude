package com.example.diaryapp.di;

import android.content.Context;
import com.example.diaryapp.notification.ThemePreferences;
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
public final class NotificationModule_ProvideThemePreferencesFactory implements Factory<ThemePreferences> {
  private final Provider<Context> contextProvider;

  private NotificationModule_ProvideThemePreferencesFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ThemePreferences get() {
    return provideThemePreferences(contextProvider.get());
  }

  public static NotificationModule_ProvideThemePreferencesFactory create(
      Provider<Context> contextProvider) {
    return new NotificationModule_ProvideThemePreferencesFactory(contextProvider);
  }

  public static ThemePreferences provideThemePreferences(Context context) {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideThemePreferences(context));
  }
}
