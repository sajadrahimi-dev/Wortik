package com.wortik.app.di

import android.content.Context
import androidx.room.Room
import com.wortik.app.data.local.UserDataDao
import com.wortik.app.data.local.UserDataDatabase
import com.wortik.app.data.local.WordDao
import com.wortik.app.data.local.WortikDatabase
import com.wortik.app.data.repository.DictionaryRepositoryImpl
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWortikDatabase(
        @ApplicationContext context: Context
    ): WortikDatabase {
        return Room.databaseBuilder(
            context,
            WortikDatabase::class.java,
            "wortik_v8.db"
        )
            .createFromAsset("databases/wortik.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDataDatabase(
        @ApplicationContext context: Context
    ): UserDataDatabase {
        return Room.databaseBuilder(
            context,
            UserDataDatabase::class.java,
            "wortik_user_data.db"
        )
            .addMigrations(UserDataDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideWordDao(database: WortikDatabase): WordDao = database.wordDao()

    @Provides
    fun provideUserDataDao(database: UserDataDatabase): UserDataDao = database.userDataDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDictionaryRepository(
        impl: DictionaryRepositoryImpl
    ): DictionaryRepository
}
