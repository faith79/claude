package com.example.diaryapp.di

import com.example.diaryapp.data.repository.AuthRepository
import com.example.diaryapp.data.repository.AuthRepositoryImpl
import com.example.diaryapp.data.repository.DiaryRepository
import com.example.diaryapp.data.repository.DiaryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository
}
