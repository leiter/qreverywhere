package cut.the.crap.qreverywhere.qrhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.ItemQrHistoryBinding
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.utils.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.getQrTypeDrawable
import cut.the.crap.qreverywhere.utils.textForHistoryList

class QrHistoryAdapter(
    val detailViewItemClicked: (QrCodeItem) -> Unit,
    val focusedPosition: (Int) -> Unit,
    private val acquireDateFormatter: AcquireDateFormatter
) : RecyclerView.Adapter<QrHistoryAdapter.QrHistoryViewHolder<ViewBinding>>()  {

    inner class QrHistoryViewHolder<VB : ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root) {
        init {
            this@QrHistoryViewHolder.itemView.setOnClickListener {
                detailViewItemClicked(differ.currentList[adapterPosition])
            }
            (binding as ItemQrHistoryBinding).historyItemImage.setOnClickListener {
                focusedPosition(adapterPosition)
            }

        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<QrCodeItem>() {
        override fun areItemsTheSame(oldItem: QrCodeItem, newItem: QrCodeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QrCodeItem, newItem: QrCodeItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun setData(list: List<QrCodeItem>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrHistoryViewHolder<ViewBinding> {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_qr_history,
            parent,
            false
        )
        return QrHistoryViewHolder(
            ItemQrHistoryBinding.bind(view)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: QrHistoryViewHolder<ViewBinding>, position: Int) {
        val run = differ.currentList[position]
        if(holder.binding is ItemQrHistoryBinding){
            bindQrItem(holder.binding, run,position)
        }
    }

    private fun bindQrItem(binding: ItemQrHistoryBinding, qrItemData: QrCodeItem, position: Int){
        with(binding){
            Glide.with(root.context).load(qrItemData.img).into(historyItemImage)
            val createdText = acquireDateFormatter.getTimeTemplate(qrItemData)
            historyItemTimestamp.text = createdText

            historyItemType.setImageResource(getQrTypeDrawable(qrItemData.textContent))
            historyItemContentPreview.text = textForHistoryList(qrItemData.textContent, root.context)
            historyItemImage.setOnClickListener {
                focusedPosition(position)
            }
        }

    }
}