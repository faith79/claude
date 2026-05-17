package com.example.diaryapp.notification;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = DailyReminderWorker.class
)
public interface DailyReminderWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.example.diaryapp.notification.DailyReminderWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      DailyReminderWorker_AssistedFactory factory);
}
