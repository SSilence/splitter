package de.aditu.splitter.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.aditu.splitter.BuildConfig
import de.aditu.splitter.R
import de.aditu.splitter.repository.BookRepository
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class InfoActivity : AppCompatActivity() {

    private val bookRepository: BookRepository by inject()

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, InfoActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.info)
        }

        version.text = BuildConfig.VERSION_NAME

        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN)
        lastmodified.text = getString(R.string.info_lastmodified, simpleDateFormat.format(Date(bookRepository.lastModified())))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
