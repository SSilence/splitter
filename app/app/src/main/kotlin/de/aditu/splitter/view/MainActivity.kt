package de.aditu.splitter.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import com.miguelcatalan.materialsearchview.MaterialSearchView
import de.aditu.splitter.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    fun showSearch() {
        search_view.showSearch()
    }

    var onSearchChangeHandler: ((q: String?) -> Unit)? = null
    var onSearchCloseHandler: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        search_view.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                onSearchChangeHandler?.invoke(newText)
                return false
            }
        })

        search_view.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                onSearchCloseHandler?.invoke()
            }

            override fun onSearchViewShown() {

            }

        })

        return true
    }

    override fun onBackPressed() {
        if (search_view.isSearchOpen) {
            search_view.closeSearch()
        } else {
            super.onBackPressed()
        }
    }
}
