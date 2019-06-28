package de.aditu.splitter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import de.aditu.splitter.BuildConfig
import de.aditu.splitter.R
import de.aditu.splitter.model.Book
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

private const val MAX_DESCRIPTION_LENGTH = 300
private const val KEY_IS_DIALOG_SHOWN = "IS_DIALOG_SHOWN"
private const val KEY_CURRENT_POSITION = "CURRENT_POSITION"

class DetailFragment : androidx.fragment.app.Fragment() {

    private val detailViewModel : DetailViewModel by viewModel()
    private var full = false
    private var isDialogShown = false
    private var currentPosition: Int = 0
    private var imageViewer: StfalconImageViewer<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailViewModel.book.observe(this, Observer<Book> { book ->
            (activity as DetailActivity?)?.setToolbarTitle(book.title ?: "")
            title.text = book.title
            description.text = book.description?.shorten(full) ?: ""
            isbn.text = book.isbn
            album.text = book.album
            genre.text = book.genre
            scenario.text = book.scenario
            painter.text = book.painter
            format.text = book.format
            pages.text = book.pages?.toString() ?: ""
            part.text = getString(R.string.detail_part_value, book.number ?: "", book.parts ?: "")
            release.text = if (book.release == null) "" else SimpleDateFormat("dd.MM.yyyy").format(Date(book.release!!))
            price.text = book.price?.formatAsPrice() ?: ""
            Picasso.get()
                .load(BuildConfig.SPLITTER_BASE_URL + book.getThumbnailUrl())
                .placeholder(R.drawable.placeholder)
                .into(cover)

            description.setOnClickListener {
                full = !full
                description.text = book.description?.shorten(full) ?: ""
            }
            val images = book.getPreviewImages()
            if(images.isEmpty() == false) {
                watch.setOnClickListener { preview(book, images) }
                watch.visibility = View.VISIBLE
            } else {
                watch.visibility = View.GONE
            }
            cover.setOnClickListener { preview(book, images) }
            title.setOnClickListener { preview(book, images) }

            if (BuildConfig.SHOW_LINK && book.url.isNullOrEmpty() == false) {
                open.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(book.url))) }
                open.visibility = View.VISIBLE
            } else {
                open.visibility = View.GONE
            }

            if (isDialogShown && imageViewer == null) {
                preview(book, images, currentPosition)
            }
        })

        val isbn = activity!!.intent.getStringExtra(BOOK_ISBN)
        detailViewModel.load(isbn)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean(KEY_IS_DIALOG_SHOWN)
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_DIALOG_SHOWN, isDialogShown)
        outState.putInt(KEY_CURRENT_POSITION, currentPosition)
        super.onSaveInstanceState(outState)
    }

    private fun preview(book: Book, images: List<String>, startPosition: Int = 0) {
        if (images.isEmpty()) {
            Toast.makeText(context, getString(R.string.detail_no_images_error), Toast.LENGTH_LONG).show()
            return
        }

        val bookInfoView = View.inflate(context, R.layout.fragment_detail_bookinfos, null)
        val bookInfoViewContainer = bookInfoView.findViewById<ConstraintLayout>(R.id.info)
        val bookInfoViewTitle = bookInfoView.findViewById<TextView>(R.id.title)
        bookInfoViewTitle.text = resources.getString(R.string.detail_book_info_title, 1, images.size, book.title , book.painter)

        val urls = images.map { BuildConfig.SPLITTER_BASE_URL + it }
        imageViewer = StfalconImageViewer.Builder<String>(context, urls) { view, image ->
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
            view.setOnClickListener {
                bookInfoViewContainer.visibility = if (bookInfoViewContainer.visibility == View.GONE) View.VISIBLE else View.GONE
            }
            Picasso.get()
                .load(image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.loading)
                .into(view)
        }
        .withStartPosition(startPosition)
        .withImageChangeListener {
            currentPosition = it
            bookInfoViewTitle.text = resources.getString(R.string.detail_book_info_title, it + 1, images.size, book.title , book.painter)
        }
        .withDismissListener { isDialogShown = false }
        .withOverlayView(bookInfoView)
        .show()

        currentPosition = startPosition
        isDialogShown = true
    }

    fun Double.formatAsPrice(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        format.setCurrency(Currency.getInstance("EUR"))
        return format.format(this)
    }

    fun String.shorten(full: Boolean) =
        if (!full && this.length > MAX_DESCRIPTION_LENGTH) this.substring(0, MAX_DESCRIPTION_LENGTH) + "..." else this
}