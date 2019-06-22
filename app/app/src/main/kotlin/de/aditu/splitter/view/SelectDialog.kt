package de.aditu.splitter.view

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.aditu.splitter.R

class SelectDialog(private val context: Context) {

    var selected: String? = null
    var onSelect: ((String?) -> Unit)? = null
    var dialog: Dialog? = null

    fun show(values: List<Pair<String, String>>, selected: String?, onSelect: (String?) -> Unit) {
        this.selected = selected
        this.onSelect = onSelect

        dialog = Dialog(context)
        val view = View.inflate(context, R.layout.dialog_select, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = SelectAdapter(values)
        dialog?.setContentView(view)
        dialog?.show()
    }

    internal class SelectViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var title = view.findViewById<View>(R.id.title) as TextView
    }

    private inner class SelectAdapter(private val values: List<Pair<String, String>>) : RecyclerView.Adapter<SelectViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dialog_select_entry, parent, false)
            return SelectViewHolder(view)
        }

        override fun getItemCount() = values.size

        override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
            val value = values[position]
            holder.title.text = value.second
            if ((position == 0 && this@SelectDialog.selected == null) || value.first == this@SelectDialog.selected) {
                holder.title.setTypeface(holder.title.getTypeface(), Typeface.BOLD)
            } else {
                holder.title.setTypeface(holder.title.getTypeface(), Typeface.NORMAL)
            }
            holder.view.setOnClickListener {
                onSelect?.invoke(if (position == 0) null else value.first)
                dialog?.dismiss()
            }
        }
    }
}