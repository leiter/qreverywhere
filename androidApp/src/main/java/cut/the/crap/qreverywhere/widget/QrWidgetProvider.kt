package cut.the.crap.qreverywhere.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.compose.ComposeMainActivity
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * App Widget Provider for displaying QR codes on the home screen
 *
 * Features:
 * - Displays the most recently saved QR code
 * - Tap to open the app at the detail screen for that QR
 * - Auto-updates when the widget is placed
 */
class QrWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val repository: QrRepository by inject()
    private val qrGenerator: QrCodeGenerator by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_qr)

            // Get the most recent QR code from history
            val recentQr = repository.getQrHistory().firstOrNull()?.firstOrNull()

            if (recentQr != null) {
                // Try to use existing image data or generate new one
                val imageData = recentQr.imageData
                    ?: qrGenerator.generateQrCode(recentQr.textContent, 200, 200)

                if (imageData != null && imageData.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.widget_qr_image, bitmap)
                    }
                }

                // Set the label text (truncated if too long)
                val labelText = recentQr.textContent.take(30) +
                    if (recentQr.textContent.length > 30) "..." else ""
                views.setTextViewText(R.id.widget_qr_label, labelText)

                // Create intent to open detail screen
                val intent = Intent(context, ComposeMainActivity::class.java).apply {
                    action = ACTION_OPEN_DETAIL
                    putExtra(EXTRA_QR_ID, recentQr.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            } else {
                // No QR code saved yet - show placeholder
                views.setImageViewResource(R.id.widget_qr_image, R.drawable.ic_qr_code_scanner)
                views.setTextViewText(R.id.widget_qr_label, context.getString(R.string.widget_no_qr))

                // Create intent to open create screen
                val intent = Intent(context, ComposeMainActivity::class.java).apply {
                    action = ACTION_OPEN_CREATE
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is disabled
    }

    companion object {
        const val ACTION_OPEN_DETAIL = "cut.the.crap.qreverywhere.OPEN_DETAIL"
        const val ACTION_OPEN_CREATE = "cut.the.crap.qreverywhere.OPEN_CREATE"
        const val ACTION_OPEN_SCAN = "cut.the.crap.qreverywhere.OPEN_SCAN"
        const val EXTRA_QR_ID = "qr_id"

        /**
         * Request update for all widgets
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, QrWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
