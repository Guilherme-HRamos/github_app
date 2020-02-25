package br.com.gabrielmarcos.githubmvvm.plugin

import br.com.gabrielmarcos.githubmvvm.model.FavModel
import br.com.gabrielmarcos.githubmvvm.utils.favExpectedValue
import br.com.gabrielmarcos.githubmvvm.utils.gistDefault
import br.com.gabrielmarcos.githubmvvm.utils.gistExpectedResponse
import br.com.gabrielmarcos.githubmvvm.utils.starredGistExpectedValue
import br.com.gabrielmarcos.githubmvvm.utils.unstarredGistExpectedValue
import io.reactivex.Completable
import io.reactivex.Single.just
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GistRepositoryImplTest {

    // TODO Implements access to room to veriry results

    @Mock
    private lateinit var gistDAO: GistDAO

    @Mock
    private lateinit var favoritesDAO: FavoritesDAO

    @Mock
    private lateinit var gistService: GitService

    private lateinit var repository: GistRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        repository = GistRepositoryImpl(gistService, gistDAO, favoritesDAO)
    }

    @Test
    fun `when get gistList local database assert result not null`() {
        `when`(gistDAO.getAllGists()).thenReturn(just(gistExpectedResponse))
        val testObserver = repository.getGistList(0, false).test()
        testObserver.assertComplete()
        testObserver.assertValue {
            assertNotNull(it)
            assertTrue(it == gistExpectedResponse)
            true
        }
    }

    @Test
    fun `when save local gist list database assert not error`() {
        `when`(gistDAO.insertAll(gistExpectedResponse)).thenReturn(Completable.complete())
        val testObserver = repository.saveLocalGist(gistExpectedResponse).test()

        testObserver.assertNoErrors()
    }

    @Test
    fun `when save favorite gist into database assert not error`() {
        `when`(favoritesDAO.insertFavGists(FavModel(0, starredGistExpectedValue.gistId)))
            .thenReturn(Completable.complete())
        val testObserver = repository.setFavoriteGist(starredGistExpectedValue).test()

        testObserver.assertNoErrors()
    }

    @Test
    fun `when delete favorite gist id into database assert not error`() {
        `when`(favoritesDAO.deleteFavById(unstarredGistExpectedValue.gistId))
            .thenReturn(Completable.complete())
        val testObserver =
            repository.deletFavoriteGistById(unstarredGistExpectedValue.gistId).test()

        testObserver.assertNoErrors()
    }

    @Test
    fun `when get gist local database assert result not null`() {
        `when`(gistDAO.getGistById(gistDefault.gistId)).thenReturn(just(gistDefault))
        val testObserver = repository.getGist(gistDefault.gistId, false).test()

        testObserver.assertComplete()
        testObserver.assertValue {
            assertNotNull(it)
            assertTrue(it == gistDefault)
            true
        }
    }

    @Test
    fun `when get favorite gists in database assert result not null`() {
        `when`(favoritesDAO.getAllFavGists()).thenReturn(just(favExpectedValue))
        val testObserver = repository.getSavedFavoriteGist().test()

        testObserver.assertComplete()
        testObserver.assertValue {
            assertNotNull(it)
            assertTrue(it == favExpectedValue)
            true
        }
    }
}