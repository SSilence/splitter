package de.aditu.splitter.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import de.aditu.splitter.R

import kotlinx.android.synthetic.main.activity_main.*

const val BOOK_ISBN = "BOOK_ISBN"

class DetailActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, isbn: String) {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(BOOK_ISBN, isbn)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (Navigation.findNavController(this, R.id.fragments).popBackStack()) {
            return true
        }
        onBackPressed()
        return true
    }

    fun setToolbarTitle(title: String) {
        toolbar.title = title
    }
}
