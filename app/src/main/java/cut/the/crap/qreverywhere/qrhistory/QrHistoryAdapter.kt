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
import cut.the.crap.qreverywhere.stuff.getQrTypeDrawable
import java.text.SimpleDateFormat
import java.util.*

class QrHistoryAdapter(val createdTemplate: String) : RecyclerView.Adapter<QrHistoryAdapter.QrHistoryViewHolder<ViewBinding>>()  {



    inner class QrHistoryViewHolder<VB : ViewBinding>(val binding: VB): RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<QrCodeItem>() {
        override fun areItemsTheSame(oldItem: QrCodeItem, newItem: QrCodeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QrCodeItem, newItem: QrCodeItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

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
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val createdText = createdTemplate.format(dateFormat.format(qrItemData.timestamp))
            historyItemTimestamp.text = createdText
            historyItemType.setImageResource(getQrTypeDrawable(qrItemData.textContent))
            historyItemContentPreview.text = qrItemData.textContent
            root.setOnClickListener {

            }
        }

    }
}