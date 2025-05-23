package acr.browser.lightning.browser.tab

import acr.browser.lightning.R
import acr.browser.lightning.extensions.desaturate
import acr.browser.lightning.extensions.dimen
import acr.browser.lightning.extensions.drawTrapezoid
import acr.browser.lightning.extensions.inflater
import acr.browser.lightning.extensions.tint
import acr.browser.lightning.utils.ThemeUtils
import acr.browser.lightning.utils.Utils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * The adapter that renders tabs in the desktop form.
 *
 * @param onClick Invoked when the tab is clicked.
 * @param onLongClick Invoked when the tab is long pressed.
 * @param onCloseClick Invoked when the tab's close button is clicked.
 */
class DesktopTabRecyclerViewAdapter(
    context: Context,
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit,
    private val onCloseClick: (Int) -> Unit,
) : ListAdapter<TabViewState, TabViewHolder>(
    object : DiffUtil.ItemCallback<TabViewState>() {
        override fun areItemsTheSame(oldItem: TabViewState, newItem: TabViewState): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TabViewState, newItem: TabViewState): Boolean =
            oldItem == newItem
    }
) {
    private val backgroundTabDrawable: Drawable
    private val foregroundTabDrawable: Drawable
    private var foregroundLayout: LinearLayout? = null

    init {
        val backgroundColor =
            Utils.mixTwoColors(ThemeUtils.getPrimaryColor(context), Color.BLACK, 0.75f)
        val backgroundTabBitmap = createBitmap(
            context.dimen(R.dimen.desktop_tab_width),
            context.dimen(R.dimen.desktop_tab_height)
        ).also {
            Canvas(it).drawTrapezoid(backgroundColor, true)
        }
        backgroundTabDrawable = backgroundTabBitmap.toDrawable(context.resources)

        val foregroundColor = ThemeUtils.getPrimaryColor(context)
        val foregroundTabBitmap = createBitmap(context.dimen(R.dimen.desktop_tab_width), context.dimen(R.dimen.desktop_tab_height)).also {
            Canvas(it).drawTrapezoid(foregroundColor, false)
        }
        foregroundTabDrawable = foregroundTabBitmap.toDrawable(context.resources).mutate()
    }

    fun updateForegroundTabColor(color: Int) {
        foregroundTabDrawable.tint(color)
        foregroundLayout?.invalidate()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TabViewHolder {
        val view =
            viewGroup.context.inflater.inflate(R.layout.tab_list_item_horizontal, viewGroup, false)
        return TabViewHolder(
            view,
            onClick = onClick,
            onLongClick = onLongClick,
            onCloseClick = onCloseClick
        )
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.exitButton.tag = position

        val tab = getItem(position)

        holder.txtTitle.text = tab.title
        updateViewHolderAppearance(holder, tab.isSelected)
        updateViewHolderFavicon(holder, tab.icon, tab.isSelected)
        updateViewHolderBackground(holder, tab.isSelected)
    }

    private fun updateViewHolderFavicon(
        viewHolder: TabViewHolder,
        favicon: Bitmap?,
        isForeground: Boolean
    ) {
        favicon?.let {
            if (isForeground) {
                viewHolder.favicon.setImageBitmap(it)
            } else {
                viewHolder.favicon.setImageBitmap(it.desaturate())
            }
        } ?: viewHolder.favicon.setImageResource(R.drawable.ic_webpage)
    }

    private fun updateViewHolderBackground(viewHolder: TabViewHolder, isForeground: Boolean) {
        if (isForeground) {
            foregroundLayout = viewHolder.layout
            viewHolder.layout.background = foregroundTabDrawable
        } else {
            viewHolder.layout.background = backgroundTabDrawable
        }
    }

    private fun updateViewHolderAppearance(
        viewHolder: TabViewHolder,
        isForeground: Boolean
    ) {
        if (isForeground) {
            TextViewCompat.setTextAppearance(viewHolder.txtTitle, R.style.boldText)
        } else {
            TextViewCompat.setTextAppearance(viewHolder.txtTitle, R.style.normalText)
        }
    }
}
