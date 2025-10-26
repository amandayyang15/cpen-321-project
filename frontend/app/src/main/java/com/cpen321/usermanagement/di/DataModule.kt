package com.cpen321.usermanagement.di

import android.content.Context
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.ProjectInterface
import com.cpen321.usermanagement.data.remote.websocket.ChatWebSocketService
import com.cpen321.usermanagement.data.repository.ProjectRepository
import com.cpen321.usermanagement.data.repository.ProjectRepositoryImpl
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideNavigationStateManager(): NavigationStateManager {
        return NavigationStateManager()
    }

    @Provides
    @Singleton
    fun provideProjectInterface(): ProjectInterface {
        return RetrofitClient.projectInterface
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectInterface: ProjectInterface
    ): ProjectRepository {
        return ProjectRepositoryImpl(projectInterface)
    }

    @Provides
    @Singleton
    fun provideChatWebSocketService(): ChatWebSocketService {
        return ChatWebSocketService()
    }
}
