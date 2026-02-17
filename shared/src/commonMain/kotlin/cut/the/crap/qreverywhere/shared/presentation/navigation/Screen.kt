package cut.the.crap.qreverywhere.shared.presentation.navigation

/**
 * Sealed class representing navigation destinations in the app
 * This is a shared navigation structure for Compose Multiplatform
 */
sealed class Screen(val route: String) {
    object History : Screen("history")
    object Scan : Screen("scan")
    object Create : Screen("create")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
    object CreateText : Screen("create/text/{qrType}") {
        fun createRoute(qrType: String = "text") = "create/text/$qrType"
    }
    object CreateEmail : Screen("create/email")
    object CreateVcard : Screen("create/vcard")
    object CreateWiFi : Screen("create/wifi")
    object CreateCalendar : Screen("create/calendar")
    object CreateLocation : Screen("create/location")
    object CreateMeCard : Screen("create/mecard")
    object Fullscreen : Screen("fullscreen/{itemId}") {
        fun createRoute(itemId: Int) = "fullscreen/$itemId"
    }
}
