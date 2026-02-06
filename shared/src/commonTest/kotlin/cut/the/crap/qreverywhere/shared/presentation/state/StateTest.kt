package cut.the.crap.qreverywhere.shared.presentation.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertIs

/**
 * Unit tests for State sealed class and extension functions
 */
class StateTest {

    // ==================== Loading State Tests ====================

    @Test
    fun `loading factory creates Loading state without data`() {
        val state: State<String> = State.loading()
        assertIs<State.Loading<String>>(state)
        assertNull(state.data)
        assertTrue(state.showLoading)
        assertEquals(0, state.progress)
    }

    @Test
    fun `loading factory creates Loading state with data`() {
        val state = State.loading(data = "cached", showLoading = true, progress = 50)
        assertIs<State.Loading<String>>(state)
        assertEquals("cached", state.data)
        assertTrue(state.showLoading)
        assertEquals(50, state.progress)
    }

    @Test
    fun `loading factory can hide loading indicator`() {
        val state = State.loading(data = "data", showLoading = false)
        assertIs<State.Loading<String>>(state)
        assertFalse(state.showLoading)
    }

    // ==================== Success State Tests ====================

    @Test
    fun `success factory creates Success state with data`() {
        val state = State.success("result")
        assertIs<State.Success<String>>(state)
        assertEquals("result", state.data)
    }

    @Test
    fun `success factory works with complex types`() {
        data class User(val name: String, val age: Int)
        val user = User("John", 30)
        val state = State.success(user)
        assertIs<State.Success<User>>(state)
        assertEquals(user, state.data)
    }

    @Test
    fun `success factory works with lists`() {
        val list = listOf(1, 2, 3)
        val state = State.success(list)
        assertIs<State.Success<List<Int>>>(state)
        assertEquals(3, state.data.size)
    }

    // ==================== Error State Tests ====================

    @Test
    fun `error factory creates Error state with message`() {
        val state: State<String> = State.error("Something went wrong")
        assertIs<State.Error<String>>(state)
        assertEquals("Something went wrong", state.message)
        assertNull(state.data)
        assertNull(state.throwable)
    }

    @Test
    fun `error factory creates Error state with throwable`() {
        val exception = RuntimeException("Test error")
        val state: State<String> = State.error("Error occurred", throwable = exception)
        assertIs<State.Error<String>>(state)
        assertEquals("Error occurred", state.message)
        assertEquals(exception, state.throwable)
    }

    @Test
    fun `error factory creates Error state from throwable`() {
        val exception = IllegalArgumentException("Invalid input")
        val state: State<String> = State.error(exception)
        assertIs<State.Error<String>>(state)
        assertEquals("Invalid input", state.message)
        assertEquals(exception, state.throwable)
    }

    @Test
    fun `error factory handles throwable with null message`() {
        val exception = RuntimeException()
        val state: State<String> = State.error(exception)
        assertIs<State.Error<String>>(state)
        assertEquals("Unknown error", state.message)
    }

    @Test
    fun `error factory can preserve previous data`() {
        val state = State.error("Error", data = "previous value")
        assertIs<State.Error<String>>(state)
        assertEquals("previous value", state.data)
    }

    // ==================== Extension Function Tests ====================

    @Test
    fun `getData returns null for Loading without data`() {
        val state: State<String> = State.loading()
        assertNull(state.getData())
    }

    @Test
    fun `getData returns data for Loading with data`() {
        val state = State.loading(data = "cached")
        assertEquals("cached", state.getData())
    }

    @Test
    fun `getData returns data for Success`() {
        val state = State.success("result")
        assertEquals("result", state.getData())
    }

    @Test
    fun `getData returns null for Error without data`() {
        val state: State<String> = State.error("Error")
        assertNull(state.getData())
    }

    @Test
    fun `getData returns data for Error with data`() {
        val state = State.error("Error", data = "fallback")
        assertEquals("fallback", state.getData())
    }

    @Test
    fun `isLoading returns true for Loading state`() {
        val state: State<String> = State.loading()
        assertTrue(state.isLoading())
        assertFalse(state.isSuccess())
        assertFalse(state.isError())
    }

    @Test
    fun `isSuccess returns true for Success state`() {
        val state = State.success("data")
        assertFalse(state.isLoading())
        assertTrue(state.isSuccess())
        assertFalse(state.isError())
    }

    @Test
    fun `isError returns true for Error state`() {
        val state: State<String> = State.error("Error")
        assertFalse(state.isLoading())
        assertFalse(state.isSuccess())
        assertTrue(state.isError())
    }

    // ==================== State Transition Tests ====================

    @Test
    fun `state can transition from Loading to Success`() {
        var state: State<String> = State.loading()
        assertTrue(state.isLoading())

        state = State.success("loaded data")
        assertTrue(state.isSuccess())
        assertEquals("loaded data", state.getData())
    }

    @Test
    fun `state can transition from Loading to Error`() {
        var state: State<String> = State.loading()
        assertTrue(state.isLoading())

        state = State.error("Failed to load")
        assertTrue(state.isError())
        assertEquals("Failed to load", (state as State.Error).message)
    }

    @Test
    fun `state preserves data through Loading with previous data`() {
        val state1 = State.success("initial")
        val state2 = State.loading(data = state1.getData())
        val state3 = State.success("updated")

        assertEquals("initial", state1.getData())
        assertEquals("initial", state2.getData())
        assertEquals("updated", state3.getData())
    }

    // ==================== UiState Tests ====================

    @Test
    fun `UiState Loading is singleton`() {
        val state1 = UiState.Loading
        val state2 = UiState.Loading
        assertTrue(state1 === state2)
    }

    @Test
    fun `UiState LoadingProgress tracks progress`() {
        val state = UiState.LoadingProgress(progress = 75, loaderId = 1)
        assertEquals(75, state.progress)
        assertEquals(1, state.loaderId)
        assertTrue(state.showLoading)
    }

    @Test
    fun `UiState Success holds list data`() {
        val state = UiState.Success(listOf("a", "b", "c"))
        assertEquals(3, state.data.size)
        assertEquals("a", state.data[0])
    }

    @Test
    fun `UiState Error holds message and throwable`() {
        val exception = RuntimeException("Test")
        val state = UiState.Error(message = "Error", throwable = exception)
        assertEquals("Error", state.message)
        assertEquals(exception, state.throwable)
    }
}
