package com.monishvm.simplenotes.di

import androidx.lifecycle.ViewModelProvider
import com.monishvm.simplenotes.util.ViewModelProviderFactory
import dagger.Binds
import dagger.Module


@Module
abstract class ViewModelFactoryModule {

    // Method #2
    @Binds
    abstract fun bindViewModelFactory(viewModelProvideFactory: ViewModelProviderFactory): ViewModelProvider.Factory
}