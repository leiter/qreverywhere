package cut.the.crap.qreverywhere.qrhistory

import android.graphics.Canvas
import android.view.ViewParent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.ItemQrHistoryBinding

class ItemDragManager(private val swipeAction: (pos: Int, direction: Int) -> Unit, dragDirs: Int, swipeDirs: Int) :
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = true


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeAction(viewHolder.adapterPosition, direction)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val vh = viewHolder as? QrHistoryAdapter.QrHistoryViewHolder<ItemQrHistoryBinding>
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(vh?.binding?.historyItemForeground)
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val vh = viewHolder as? QrHistoryAdapter.QrHistoryViewHolder<ItemQrHistoryBinding>

        ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(
            c, recyclerView, vh?.binding?.historyItemForeground,
            dX, dY, actionState, isCurrentlyActive
        )
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val dragView = if (viewHolder.itemView is ViewParent) {
            viewHolder.itemView.findViewById(R.id.history_item_foreground) ?: viewHolder.itemView
        } else {
            viewHolder.itemView
        }
        dragView.let {
            ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                c, recyclerView, dragView,
                dX, dY, actionState, isCurrentlyActive
            )
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val vh = viewHolder as? QrHistoryAdapter.QrHistoryViewHolder<ItemQrHistoryBinding>
        ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(vh?.binding?.historyItemForeground)
    }
}