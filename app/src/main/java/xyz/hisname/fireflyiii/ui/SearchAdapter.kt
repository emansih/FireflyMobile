package xyz.hisname.fireflyiii.ui

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import xyz.hisname.fireflyiii.R

class SearchAdapter(context: Context, cursor: Cursor): CursorAdapter(context, cursor, false) {

    private lateinit var searchedItem: TextView
    private lateinit var searchedItemType: TextView

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.search_items, parent, false)
        searchedItem = view.findViewById(R.id.searchedItem)
        searchedItemType = view.findViewById(R.id.searchedItemType)
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val searchedResult = cursor.getString(1)
        val searchedResultType = cursor.getString(2)
        searchedItem.text = searchedResult
        searchedItemType.text = searchedResultType
    }
}