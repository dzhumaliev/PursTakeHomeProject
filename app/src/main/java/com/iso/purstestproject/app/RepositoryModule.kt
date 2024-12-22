package com.iso.purstestproject.app

import com.iso.data.network.datasource.TimesServerRepository
import com.iso.domain.ui.usecase.TimeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class RepositoryModule {


    @Provides
    fun provideTimeUseCase(repository: TimesServerRepository) : TimeUseCase{
        return TimeUseCase(repository)
    }

}