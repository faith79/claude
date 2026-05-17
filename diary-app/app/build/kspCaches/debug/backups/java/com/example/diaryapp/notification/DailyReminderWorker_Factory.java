package com.example.diaryapp.notification;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
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
public final class DailyReminderWorker_Factory {
  public DailyReminderWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params);
  }

  public static DailyReminderWorker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DailyReminderWorker newInstance(Context context, WorkerParameters params) {
    return new DailyReminderWorker(context, params);
  }

  private static final class InstanceHolder {
    static final DailyReminderWorker_Factory INSTANCE = new DailyReminderWorker_Factory();
  }
}
