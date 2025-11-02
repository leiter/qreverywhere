package cut.the.crap.qreverywhere.compose.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cut.the.crap.qreverywhere.R

/**
 * Navigation destinations for Compose UI
 * Maps to the existing fragment navigation structure
 */
sealed class ComposeScreen(
    val route: String,
    @StringRes val title: Int,
@DrawableRes val icon: Int
) {
    // Main bottom navigation screens
    object Scan : ComposeScreen(
        route = "scan",
        title = R.string.nav_title_scan,
        icon = R.drawable.ic_videocam
    )

    object Create : ComposeScreen(
        route = "create",
        title = R.string.nav_title_create,
        icon = R.drawable.ic_create_qr
    )

    object History : ComposeScreen(
        route = "history",
        title = R.string.nav_title_history,
        icon = R.drawable.ic_history
    )

    // Create sub-screens
    object CreateText : ComposeScreen(
        route = "create/text/{qrType}",
        title = R.string.nav_title_create,
        icon = R.drawable.ic_create_qr
    ) {
        fun createRoute(qrType: String) = "create/text/$qrType"
    }

    object CreateEmail : ComposeScreen(
        route = "create/email",
        title = R.string.create_title_email,
        icon = R.drawable.ic_mail_outline
    )

    // Detail screens
    object DetailView : ComposeScreen(
        route = "detail/{originFlag}",
        title = R.string.nav_title_scan,
        icon = R.drawable.ic_videocam
    ) {
        fun createRoute(originFlag: Int) = "detail/$originFlag"
    }

    object Fullscreen : ComposeScreen(
        route = "fullscreen/{originFlag}",
        title = R.string.nav_title_scan,
        icon = R.drawable.ic_videocam
    ) {
        fun createRoute(originFlag: Int) = "fullscreen/$originFlag"
    }

    // Settings
    object Settings : ComposeScreen(
        route = "settings",
        title = R.string.nav_title_scan, // TODO: Add settings string resource
        icon = R.drawable.ic_file_open
    )
}
