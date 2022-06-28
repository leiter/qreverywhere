package cut.the.crap.qreverywhere.qrhistory

import android.content.Context
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
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.getQrTypeDrawable
import java.text.SimpleDateFormat
import java.util.*

class QrHistoryAdapter(context: Context, val detailViewItemClicked: (QrCodeItem) -> Unit)

    : RecyclerView.Adapter<QrHistoryAdapter.QrHistoryViewHolder<ViewBinding>>()  {

    private val createdTemplate: String = context.getString(R.string.qr_created_on_template)
    private val scannedTemplate: String = context.getString(R.string.qr_scanned_on_template)
    private val loadedFromFileTemplate: String = context.getString(R.string.qr_from_file_on_template)

    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

    inner class QrHistoryViewHolder<VB : ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root) {
        init {
            this@QrHistoryViewHolder.itemView.setOnClickListener {
                detailViewItemClicked(differ.currentList[adapterPosition])
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

    private fun getTimeTemplate(qrItemData: QrCodeItem) : String {
        return when(qrItemData.acquireType){
            Acquire.SCANNED -> scannedTemplate
            Acquire.CREATED -> createdTemplate
            Acquire.FROM_FILE -> loadedFromFileTemplate
            else -> createdTemplate
        }
    }

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
            bindQrItem(holder.binding, run)
        }
    }

    private fun bindQrItem(binding: ItemQrHistoryBinding, qrItemData: QrCodeItem){

        with(binding){
            Glide.with(root.context).load(qrItemData.img).into(historyItemImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = qrItemData.timestamp
            }

            val createdText = getTimeTemplate(qrItemData).format(dateFormat.format(qrItemData.timestamp))
            historyItemTimestamp.text = createdText
            historyItemType.setImageResource(getQrTypeDrawable(qrItemData.textContent))
            historyItemContentPreview.text = qrItemData.textContent
        }

    }
}