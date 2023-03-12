package cut.the.crap.qreverywhere.qrhistory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.databinding.ItemQrHistoryBinding
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.ProtocolPrefix
import cut.the.crap.qreverywhere.utils.QrCodeType
import cut.the.crap.qreverywhere.utils.determineType
import cut.the.crap.qreverywhere.utils.isVcard
import cut.the.crap.qreverywhere.utils.ui.isLandscape
import cut.the.crap.qrrepository.QrItem

class QrHistoryAdapter(
    val detailViewItemClicked: (QrItem) -> Unit,
    val deleteItemClicked: (Int) -> Unit,
    val fullscreenItemClicked: (Int) -> Unit,
    private val acquireDateFormatter: AcquireDateFormatter,
) : RecyclerView.Adapter<QrHistoryAdapter.QrHistoryViewHolder<ViewBinding>>() {

    inner class QrHistoryViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
        init {
            this@QrHistoryViewHolder.itemView.setOnClickListener {
                detailViewItemClicked(differ.currentList[adapterPosition])
            }
            (binding as ItemQrHistoryBinding).historyItemImage.setOnClickListener {
                fullscreenItemClicked(adapterPosition)
            }

        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<QrItem>() {
        override fun areItemsTheSame(oldItem: QrItem, newItem: QrItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QrItem, newItem: QrItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun setData(list: List<QrItem>) = differ.submitList(list)

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
        val qrItem = differ.currentList[position]
        if (holder.binding is ItemQrHistoryBinding) {
            bindQrItem(holder.binding, qrItem, position)
        }
    }

    private fun bindQrItem(binding: ItemQrHistoryBinding, qrItemData: QrItem, position: Int) {
        with(binding) {
            Glide.with(root.context).load(qrItemData.img).into(historyItemImage)
            val createdText = acquireDateFormatter.getTimeTemplate(qrItemData)
            historyItemTimestamp.text = createdText

            historyItemType.setImageResource(getQrTypeDrawable(qrItemData.textContent))
            historyItemContentPreview.text = textForHistoryList(qrItemData.textContent, root.context)
            historyItemImage.setOnClickListener {
                fullscreenItemClicked(position)
            }
            if (root.context.isLandscape()){
                historyItemFullscreen?.setOnClickListener { fullscreenItemClicked(position) }
                historyItemDelete?.setOnClickListener { deleteItemClicked(position) }
            }

        }
    }

    private fun getQrTypeDrawable(contentString: String): Int {
        val decoded = Uri.decode(contentString)

        return when {
            decoded.startsWith(ProtocolPrefix.TEL) -> R.drawable.ic_phone
            decoded.startsWith(ProtocolPrefix.MAILTO) -> R.drawable.ic_mail_outline
            decoded.startsWith(ProtocolPrefix.HTTP) -> R.drawable.ic_open_in_browser
            decoded.startsWith(ProtocolPrefix.HTTPS) -> R.drawable.ic_open_in_browser
            decoded.startsWith(ProtocolPrefix.SMS) -> R.drawable.ic_sms
            decoded.startsWith(ProtocolPrefix.SMSTO) -> R.drawable.ic_sms
            isVcard(decoded) -> R.drawable.ic_add_contact
            else -> R.drawable.ic_content
        }
    }

    private fun textForHistoryList(text: String, context: Context): String {
        val decodedText = Uri.decode(text)
        return when (determineType(decodedText)) {
            QrCodeType.PHONE -> context.getString(R.string.phone_template).format(decodedText.subSequence(4, decodedText.length - 1))
            QrCodeType.EMAIL -> context.getString(R.string.mail_template).format(decodedText.subSequence(7, decodedText.indexOf("?")))
            QrCodeType.WEB_URL -> context.getString(R.string.open_in_browser_template).format(decodedText)
            QrCodeType.CONTACT -> context.getString(R.string.contact_of_template).format(decodedText)
            QrCodeType.UNKNOWN_CONTENT -> context.getString(R.string.text_template).format(decodedText)
            else -> context.getString(R.string.text_template).format(decodedText)
        }
    }
}