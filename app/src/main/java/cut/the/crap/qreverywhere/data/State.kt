package cut.the.crap.qreverywhere.data

sealed class State<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T?) : State<T>(data)

    class Loading<T>(data: T? = null, val showLoading: Boolean = false, val progress: Int = 0) : State<T>(data)

    class Error<T>(message: String?, data: T? = null, val cause: Throwable? = null) : State<T>(data, message)

    companion object {

        fun <T> loading(): State<T> {
            return Loading()
        }

        fun <T> showLoading(data: T? = null, showLoading: Boolean = true): State<T> {
            return Loading(data, showLoading)
        }

        fun <T> success(): State<T> {
            return Success(null)
        }

        fun <T> success(data: T?): State<T> {
            return Success(data)
        }

        fun <T> error(message: String? = null, data: T? = null, error: Throwable? = null): State<T> {
            return Error(message, data, error)
        }

        fun <T> error(error: Throwable): State<T> {
            return Error(null,null, error)
        }
    }
}