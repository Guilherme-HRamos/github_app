package br.com.gabrielmarcos.githubmvvm.gist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import br.com.gabrielmarcos.githubmvvm.core.TrampolineSchedulerRule
import br.com.gabrielmarcos.githubmvvm.utils.favExpectedResponse
import br.com.gabrielmarcos.githubmvvm.utils.getOrAwaitValue
import br.com.gabrielmarcos.githubmvvm.utils.gistExpectedResponse
import br.com.gabrielmarcos.githubmvvm.utils.starredGistExpectedValue
import br.com.gabrielmarcos.githubmvvm.utils.unstarredGistExpectedValue
import io.reactivex.Completable
import io.reactivex.Single.error
import io.reactivex.Single.just
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

private const val FIRST_PAGE = 0
private const val SECOND_PAGE = 1
private const val EXPECTED_FAV_LIST_SIZE = 3

@RunWith(MockitoJUnitRunner::class)
class GistViewModelTest {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var gistRepository: GistRepository

    private lateinit var viewModel: GistViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = GistViewModel(gistRepository)
    }

    @Test
    fun `when remote gist list response success then assert no errors`() {
        `when`(gistRepository.getGistList(FIRST_PAGE, true)).thenReturn(just(gistExpectedResponse))
        `when`(gistRepository.getSavedFavoriteGist()).thenReturn(favExpectedResponse)
        `when`(gistRepository.saveLocalGist(emptyList())).thenReturn(Completable.complete())

        viewModel.connectionAvailability = true
        viewModel.getGistList()
        assertGistResultList()
    }

    @Test
    fun `when remote gist list response error then assert that snack error is showed`() {
        val expectedError = RuntimeException("RuntimeException")
        `when`(gistRepository.getGistList(FIRST_PAGE, true)).thenReturn(error(expectedError))

        viewModel.connectionAvailability = true
        viewModel.getGistList()

        verify(gistRepository, times(1)).getGistList(FIRST_PAGE, true)
        verify(gistRepository, times(0)).getSavedFavoriteGist()
        verify(gistRepository, times(0)).saveLocalGist(emptyList())

        assertHandleErrorResult(expectedError.message ?: "")
    }

    @Test
    fun `when remote gist list is empty then assert empty layout is showed`() {
        `when`(gistRepository.getGistList(FIRST_PAGE, true)).thenReturn(just(emptyList()))

        viewModel.connectionAvailability = true
        viewModel.getGistList()

        verify(gistRepository, times(1)).getGistList(FIRST_PAGE, true)
        verify(gistRepository, times(0)).getSavedFavoriteGist()
        verify(gistRepository, times(0)).saveLocalGist(emptyList())

        assertTrue(viewModel.listResult.isEmpty())
        viewModel.emptyResult.getOrAwaitValue().run { assertNotNull(this) }
    }

    @Test
    fun `when local favorite gist list response success then assert that favorite id list has the same size`() {
        `when`(gistRepository.getSavedFavoriteGist()).thenReturn(favExpectedResponse)
        `when`(gistRepository.saveLocalGist(emptyList())).thenReturn(Completable.complete())

        viewModel.connectionAvailability = true
        viewModel.getLocalFavoriteList()

        verify(gistRepository, times(1)).getSavedFavoriteGist()

        assertTrue(viewModel.favIdList.size == EXPECTED_FAV_LIST_SIZE)

        viewModel.gistListViewState.getOrAwaitValue().run { assertNotNull(this) }

        viewModel.resultSuccess.getOrAwaitValue().run { assertNotNull(this) }
    }

    @Test
    fun `when remote gist response success then assert no errors`() {
        `when`(gistRepository.getGist("", true)).thenReturn(just(starredGistExpectedValue))

        viewModel.connectionAvailability = true
        viewModel.getGist("")

        verify(gistRepository, times(1)).getGist("", true)

        viewModel.gistDetailViewState.getOrAwaitValue().run {
            assertNotNull(this)
            assertTrue(this == starredGistExpectedValue)
        }

        viewModel.resultSuccess.getOrAwaitValue().run {
            assertNotNull(this)
        }
    }

    @Test
    fun `when remote gist response error then assert that snack is showed`() {
        val expectedError = RuntimeException("RuntimeException")
        `when`(gistRepository.getGist("", true)).thenReturn(error(expectedError))

        viewModel.connectionAvailability = true
        viewModel.getGist("")

        verify(gistRepository, times(1)).getGist("", true)

        assertHandleErrorResult(expectedError.message ?: "")
    }

    @Test
    fun `when save local response success then assert no errors`() {
        `when`(gistRepository.saveLocalGist(emptyList())).thenReturn(Completable.complete())
        viewModel.connectionAvailability = true
        viewModel.saveLocalResponse(emptyList())

        verify(gistRepository, times(1)).saveLocalGist(emptyList())
    }

    @Test
    fun `when gist was not starred then added gist in local database`() {
        `when`(gistRepository.setFavoriteGist(starredGistExpectedValue))
            .thenReturn(Completable.complete())
        viewModel.connectionAvailability = true
        viewModel.handleFavoriteState(starredGistExpectedValue)

        verify(gistRepository, times(1)).setFavoriteGist(starredGistExpectedValue)
        verify(gistRepository, times(0)).deletFavoriteGistById("")
    }

    @Test
    fun `when gist was starred then delete gist in local database`() {
        `when`(gistRepository.deletFavoriteGistById(unstarredGistExpectedValue.gistId)).thenReturn(Completable.complete())
        viewModel.connectionAvailability = true
        viewModel.handleFavoriteState(unstarredGistExpectedValue)

        verify(gistRepository, times(0)).setFavoriteGist(unstarredGistExpectedValue)
        verify(gistRepository, times(1)).deletFavoriteGistById(unstarredGistExpectedValue.gistId)
    }

    @Test
    fun `when update gist list then update current position assert that not null`() {
        `when`(gistRepository.getGistList(SECOND_PAGE, true)).thenReturn(just(gistExpectedResponse))
        `when`(gistRepository.getSavedFavoriteGist()).thenReturn(favExpectedResponse)
        `when`(gistRepository.saveLocalGist(emptyList())).thenReturn(Completable.complete())

        viewModel.currentPage = FIRST_PAGE
        viewModel.connectionAvailability = true
        viewModel.updateGistList()

        assertEquals(viewModel.currentPage, SECOND_PAGE)
        assertGistResultList(page = SECOND_PAGE)
    }

    private fun assertGistResultList(page: Int = FIRST_PAGE) {
        verify(gistRepository, times(1)).getGistList(page, true)
        verify(gistRepository, times(1)).getSavedFavoriteGist()
        verify(gistRepository, times(1)).saveLocalGist(emptyList())

        viewModel.showLoading.getOrAwaitValue().run {
            assertNotNull(getContentIfNotHandled())
        }

        viewModel.gistListViewState.getOrAwaitValue().run {
            assertNotNull(this)
        }

        viewModel.resultSuccess.getOrAwaitValue().run {
            assertNotNull(this)
        }
    }

    private fun assertHandleErrorResult(expectedErrorMessage: String) {
        viewModel.resultError.getOrAwaitValue().run {
            assertNotNull(this)
        }

        viewModel.showSnackbarMessage.getOrAwaitValue().run {
            assertNotNull(this)
            assertTrue(this.peekContent() == expectedErrorMessage)
        }
    }
}
