package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import cut.the.crap.qreverywhere.core.base.generated.resources.Res
import cut.the.crap.qreverywhere.core.base.generated.resources.history_type_created
import cut.the.crap.qreverywhere.core.base.generated.resources.history_type_from_file
import cut.the.crap.qreverywhere.core.base.generated.resources.history_type_scanned
import cut.the.crap.qreverywhere.core.base.generated.resources.history_type_unknown
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import org.jetbrains.compose.resources.stringResource

/**
 * Human-readable name for how a QR code was acquired.
 *
 * Screens used to show `AcquireType.name` directly, which put the raw enum
 * ("CREATED", "SCANNED") on screen in every language. Never show the enum to
 * users — it is serialisation, not copy.
 */
@Composable
fun AcquireType.localizedLabel(): String = stringResource(
    when (this) {
        AcquireType.SCANNED -> Res.string.history_type_scanned
        AcquireType.CREATED -> Res.string.history_type_created
        AcquireType.FROM_FILE -> Res.string.history_type_from_file
        AcquireType.ERROR_OCCURRED, AcquireType.EMPTY_DEFAULT -> Res.string.history_type_unknown
    }
)
