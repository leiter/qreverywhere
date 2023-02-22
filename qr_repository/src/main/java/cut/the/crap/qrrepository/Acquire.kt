package cut.the.crap.qrrepository

import androidx.annotation.IntDef

object Acquire {
    const val SCANNED = 0
    const val CREATED = 1
    const val FROM_FILE = 2
    const val ERROR_OCCURRED = 3
    const val EMPTY_DEFAULT = 4

    @IntDef(SCANNED, CREATED, FROM_FILE, ERROR_OCCURRED, EMPTY_DEFAULT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}