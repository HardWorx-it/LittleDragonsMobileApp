package com.example.littledragons.di

import com.example.littledragons.model.service.AuthService
import com.example.littledragons.model.service.AuthServiceImpl
import com.example.littledragons.model.service.EventsRepository
import com.example.littledragons.model.service.EventsRepositoryImpl
import com.example.littledragons.model.service.GradesRepository
import com.example.littledragons.model.service.GradesRepositoryImpl
import com.example.littledragons.model.service.NotificationsRepository
import com.example.littledragons.model.service.NotificationsRepositoryImpl
import com.example.littledragons.model.service.SchedulesRepository
import com.example.littledragons.model.service.SchedulesRepositoryImpl
import com.example.littledragons.model.service.SchoolClassesRepository
import com.example.littledragons.model.service.SchoolClassesRepositoryImpl
import com.example.littledragons.model.service.SchoolSubjectsRepository
import com.example.littledragons.model.service.SchoolSubjectsRepositoryImpl
import com.example.littledragons.model.service.StudentsRepository
import com.example.littledragons.model.service.StudentsRepositoryImpl
import com.example.littledragons.model.service.UserRepository
import com.example.littledragons.model.service.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Singleton
    @Binds
    abstract fun bindAuthService(repo: AuthServiceImpl): AuthService

    @Singleton
    @Binds
    abstract fun bindUserRepository(repo: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    abstract fun bindSchoolClassesRepository(repo: SchoolClassesRepositoryImpl): SchoolClassesRepository

    @Singleton
    @Binds
    abstract fun bindStudentRepository(repo: StudentsRepositoryImpl): StudentsRepository

    @Singleton
    @Binds
    abstract fun bindEventsRepository(repo: EventsRepositoryImpl): EventsRepository

    @Singleton
    @Binds
    abstract fun bindSchedulesRepository(repo: SchedulesRepositoryImpl): SchedulesRepository

    @Singleton
    @Binds
    abstract fun bindSchoolSubjectsRepository(repo: SchoolSubjectsRepositoryImpl): SchoolSubjectsRepository

    @Singleton
    @Binds
    abstract fun bindGradesRepository(repo: GradesRepositoryImpl): GradesRepository

    @Singleton
    @Binds
    abstract fun bindNotificationsRepository(repo: NotificationsRepositoryImpl): NotificationsRepository
}