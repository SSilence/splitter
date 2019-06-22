package de.aditu.splitter

import android.app.Application
import androidx.room.Room
import de.aditu.splitter.http.BackendClient
import de.aditu.splitter.repository.BookRepository
import de.aditu.splitter.repository.SharedPreferencesRepository
import de.aditu.splitter.room.AppDatabase
import de.aditu.splitter.view.DetailViewModel
import de.aditu.splitter.view.ListViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val HTTP_TIMEOUT = 15

class App : Application() {

    override fun onCreate(){
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(mainModule)
        }
    }

}

val mainModule : Module = module {
    viewModel { ListViewModel(get()) }
    viewModel { DetailViewModel(get()) }
    single { SharedPreferencesRepository(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            AppDatabase::class.java,
            "splitter"
        ).fallbackToDestructiveMigration()
            .build()
    }
    single { BookRepository(androidContext().applicationContext, get(), get(), get<AppDatabase>().bookDao())}
    single {
        val okHttpClientBuilder = OkHttpClient().newBuilder()
            .connectTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.HEADERS
            okHttpClientBuilder.addInterceptor(interceptor)
        }

        val okHttpClient = okHttpClientBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(BackendClient::class.java)
    }
}