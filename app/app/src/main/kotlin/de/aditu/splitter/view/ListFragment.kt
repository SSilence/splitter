package de.aditu.splitter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.aditu.splitter.BuildConfig
import de.aditu.splitter.R
import de.aditu.splitter.model.Book
import de.aditu.splitter.model.BookSort
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.GridLayoutManager
import java.text.SimpleDateFormat
import java.util.*

private const val STATE_GENRE = "STATE_GENRE"
private const val STATE_Q = "STATE_Q"
private const val STATE_SORT = "STATE_SORT"
private const val STATE_SCROLLPOSITION = "STATE_SCROLLPOSITION"

class ListFragment : androidx.fragment.app.Fragment() {

    private  val listViewModel : ListViewModel by viewModel()
    private var genres: List<String>? = null
    private var genre: String? = null
    private var q: String? = null
    private var bookSort: BookSort = BookSort.RELEASE_DESC
    private var isFabMenuOpen = false
    private var scrollPosition: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_list, container, false)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setRetainInstance(true)
        restoreFromState(state)

        listViewModel.books.observe(this, Observer<List<Book>> { books ->
            bookRecyclerView.setHasFixedSize(true)
            bookRecyclerView.layoutManager = GridLayoutManager(context, context!!.resources.getInteger(R.integer.number_of_grid_items))
            bookRecyclerView.adapter = BooksAdapter(books)

            if (books.isEmpty() && this.q == null && this.genre == null) {
                loading.visibility = View.VISIBLE
            } else {
                loading.visibility = View.GONE
            }

            if (books.isNotEmpty() && scrollPosition != null) {
                bookRecyclerView.scrollToPosition(scrollPosition!!)
                scrollPosition = null
            }
        })

        listViewModel.genres.observe(this, Observer<List<String>> {
            genres = (listOf(context!!.getString(R.string.filter_all)) + it).filter { it != null }
        })

        listViewModel.refreshBooksInDatabase()
    }

    override fun onSaveInstanceState(state: Bundle) {
        state.putString(STATE_GENRE, genre)
        state.putString(STATE_Q, q)
        state.putString(STATE_SORT, bookSort.toString())
        val scrollPosition = (bookRecyclerView?.getLayoutManager() as GridLayoutManager?)?.findFirstCompletelyVisibleItemPosition() ?: 0
        state.putInt(STATE_SCROLLPOSITION, scrollPosition)
        super.onSaveInstanceState(state)
    }

    override fun onViewStateRestored(state: Bundle?) {
        super.onViewStateRestored(state)
        restoreFromState(state)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).onSearchChangeHandler = { q ->
            this.q = q
            listViewModel.search(this.q, this.genre, this.bookSort)
        }

        (activity as MainActivity).onSearchCloseHandler = {
            this.q = null
            listViewModel.search(this.q, this.genre, this.bookSort)
        }

        sort.setOnClickListener {
            val bookSortValues = BookSort.values().map { it.toString() to context!!.getString(it.string) }
            SelectDialog(context!!).show(bookSortValues, this.bookSort.toString()) {
                this.bookSort = if (it != null) BookSort.valueOf(it) else BookSort.RELEASE_DESC
                bookRecyclerView.setIndexBarVisibility(this.bookSort == BookSort.TITLE_ASC)
                refresh()
            }
        }

        filter.setOnClickListener {
            SelectDialog(context!!).show(genres?.map { it to it } ?: emptyList(), genre) {
                this.genre = it
                refresh()
            }
        }

        search.setOnClickListener {
            (activity as MainActivity).showSearch()
            fabClose()
        }

        info.setOnClickListener {
            InfoActivity.start(context!!)
            fabClose()
        }

        menu.setOnClickListener { if (isFabMenuOpen) fabClose() else fabOpen() }

        bookRecyclerView.setIndexBarTransparentValue(0.0f)
        bookRecyclerView.setIndexBarTextColor(R.color.primary)
        bookRecyclerView.setIndexBarVisibility(false)

        refresh()
    }

    private fun restoreFromState(state: Bundle?) {
        if (state != null) {
            genre = state.getString(STATE_GENRE)
            q = state.getString(STATE_Q)
            val bookStateStr = state.getString(STATE_SORT)
            if (bookStateStr != null) {
                bookSort = BookSort.valueOf(bookStateStr)
            }
            scrollPosition = state.getInt(STATE_SCROLLPOSITION)
        }
    }

    private fun fabOpen() {
        ViewCompat.animate(menu)
            .rotation(90.0f)
            .withLayer()
            .setDuration(300)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
        val anim = AnimationUtils.loadAnimation(context!!, R.anim.fab_open)
        sort.startAnimation(anim)
        filter.startAnimation(anim)
        search.startAnimation(anim)
        info.startAnimation(anim)
        isFabMenuOpen = true
    }

    private fun fabClose() {
        ViewCompat.animate(menu)
            .rotation(0.0f)
            .withLayer()
            .setDuration(300)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
        val anim = AnimationUtils.loadAnimation(context!!, R.anim.fab_close)
        sort.startAnimation(anim)
        filter.startAnimation(anim)
        search.startAnimation(anim)
        info.startAnimation(anim)
        isFabMenuOpen = false
    }

    private fun refresh() {
        listViewModel.search(this.q, this.genre, this.bookSort)
        if (genre == null) {
            genreTitle.visibility = View.GONE
        } else {
            genreTitle.text = getString(R.string.filter_title, genre)
            genreTitle.visibility = View.VISIBLE
        }
    }
    
    internal class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var title = view.findViewById<View>(R.id.title) as TextView
        var release = view.findViewById<View>(R.id.release) as TextView
        var image = view.findViewById<View>(R.id.image) as ImageView
    }

    private inner class BooksAdapter(private val books: List<Book>) : RecyclerView.Adapter<ViewHolder>(), SectionIndexer {

        var sectionPositions = mutableListOf<Int>()

        override fun getSections(): Array<String> {
            val sections = mutableListOf<String>()
            sectionPositions = mutableListOf()

            books.forEachIndexed { index, book ->
                val section = book.title?.substring(0, 1)?.toUpperCase() ?: ""
                if (!sections.contains(section) && section.matches("[a-zA-Z]".toRegex())) {
                    sections.add(section)
                    sectionPositions.add(index)
                }
            }
            return sections.toTypedArray()
        }
        override fun getSectionForPosition(position: Int) = 0
        override fun getPositionForSection(sectionIndex: Int) = sectionPositions[sectionIndex]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_list_entry, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = books.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val book = books[position]
            holder.title.text = book.title

            if (book.release ?: 0 > Date().time) {
                holder.release.visibility = View.VISIBLE
                holder.release.text = SimpleDateFormat("MM.yyyy", Locale.GERMAN).format(book.release)
            } else
                holder.release.visibility = View.GONE

            Picasso.get()
                .load(BuildConfig.SPLITTER_BASE_URL + book.getThumbnailUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.image)

            holder.view.setOnClickListener {
                DetailActivity.start(context!!, book.isbn)
            }
        }
    }

}