package com.cpen321.usermanagement.di

import com.cpen321.usermanagement.data.remote.api.AuthInterface
import com.cpen321.usermanagement.data.remote.api.ExpenseInterface
import com.cpen321.usermanagement.data.remote.api.ImageInterface
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.api.TaskInterface
import com.cpen321.usermanagement.data.remote.api.UserInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthService(): AuthInterface {
        return RetrofitClient.authInterface
    }

    @Provides
    @Singleton
    fun provideUserService(): UserInterface {
        return RetrofitClient.userInterface
    }

    @Provides
    @Singleton
    fun provideMediaService(): ImageInterface {
        return RetrofitClient.imageInterface
    }


    @Provides
    @Singleton
    fun provideExpenseService(): ExpenseInterface {
        return RetrofitClient.expenseInterface
    }

    @Provides
    @Singleton
    fun provideTaskService(): TaskInterface {
        return RetrofitClient.taskInterface
    }
}
