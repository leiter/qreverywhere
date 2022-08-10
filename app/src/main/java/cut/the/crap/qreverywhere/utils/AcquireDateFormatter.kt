package cut.the.crap.qreverywhere.utils

import android.content.Context
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.db.QrCodeItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AcquireDateFormatter @Inject constructor(@ApplicationContext context: Context) {
    private val createdTemplate: String = context.getString(R.string.qr_created_on_template)
    private val scannedTemplate: String = context.getString(R.string.qr_scanned_on_template)
    private val loadedFromFileTemplate: String = context.getString(R.string.qr_from_file_on_template)

    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

    fun getTimeTemplate(qrItemData: QrCodeItem) : String {
        return when(qrItemData.acquireType){
            Acquire.SCANNED -> scannedTemplate
            Acquire.CREATED -> createdTemplate
            Acquire.FROM_FILE -> loadedFromFileTemplate
            else -> createdTemplate
        }.format(dateFormat.format(qrItemData.timestamp))
    }
}