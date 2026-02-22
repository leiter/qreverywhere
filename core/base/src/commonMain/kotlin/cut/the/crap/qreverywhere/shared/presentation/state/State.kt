package cut.the.crap.qreverywhere.shared.presentation.state

/**
 * Unified State wrapper for KMP
 *
 * This provides a consistent way to represent UI states across all platforms.
 * Supports loading, success, and error states with optional data and metadata.
 *
 * Usage:
 * ```kotlin
 * val state: StateFlow<State<String>> = ...
 *
 * when (val currentState = state.value) {
 *     is State.Loading -> showLoadingSpinner(currentState.progress)
 *     is State.Success -> showData(currentState.data)
 *     is State.Error -> showError(currentState.message, currentState.throwable)
 * }
 * ```
 */
sealed class State<out T> {

    /**
     * Loading state - indicates an operation is in progress
     * @param data Optional data from previous state to display while loading
     * @param showLoading Whether to show loading indicator (default: true)
     * @param progress Optional progress value 0-100 for progress indicators
     */
    data class Loading<T>(
        val data: T? = null,
        val showLoading: Boolean = true,
        val progress: Int = 0
    ) : State<T>()

    /**
     * Success state - indicates operation completed successfully
     * @param data The result data
     */
    data class Success<T>(val data: T) : State<T>()

    /**
     * Error state - indicates operation failed
     * @param message Human-readable error message
     * @param data Optional data from previous state to display alongside error
     * @param throwable Optional exception/throwable for debugging
     */
    data class Error<T>(
        val message: String,
        val data: T? = null,
        val throwable: Throwable? = null
    ) : State<T>()

    companion object {
        /**
         * Create a simple loading state with no data
         */
        fun <T> loading(): State<T> = Loading()

        /**
         * Create a loading state with optional previous data
         */
        fun <T> loading(data: T? = null, showLoading: Boolean = true, progress: Int = 0): State<T> =
            Loading(data, showLoading, progress)

        /**
         * Create a success state with data
         */
        fun <T> success(data: T): State<T> = Success(data)

        /**
         * Create an error state
         */
        fun <T> error(
            message: String,
            data: T? = null,
            throwable: Throwable? = null
        ): State<T> = Error(message, data, throwable)

        /**
         * Create an error state from throwable
         */
        fun <T> error(throwable: Throwable, data: T? = null): State<T> =
            Error(throwable.message ?: "Unknown error", data, throwable)
    }
}

/**
 * Alternative UI state pattern for simpler use cases
 * Use this when you don't need to preserve data across states
 */
sealed interface UiState<out T> {
    /**
     * Simple loading state
     */
    object Loading : UiState<Nothing>

    /**
     * Loading with progress tracking
     */
    data class LoadingProgress(
        val progress: Int = 0,
        val showLoading: Boolean = true,
        val loaderId: Int = -1
    ) : UiState<Nothing>

    /**
     * Success with list data
     */
    data class Success<T>(val data: List<T>) : UiState<T>

    /**
     * Error state
     */
    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null
    ) : UiState<Nothing>
}

/**
 * Extension function to get data from any state
 */
fun <T> State<T>.getData(): T? = when (this) {
    is State.Loading -> data
    is State.Success -> data
    is State.Error -> data
}

/**
 * Extension function to check if state is loading
 */
fun <T> State<T>.isLoading(): Boolean = this is State.Loading

/**
 * Extension function to check if state is success
 */
fun <T> State<T>.isSuccess(): Boolean = this is State.Success

/**
 * Extension function to check if state is error
 */
fun <T> State<T>.isError(): Boolean = this is State.Error
