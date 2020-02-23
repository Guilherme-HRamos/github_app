package br.com.gabrielmarcos.githubmvvm.gist

import androidx.lifecycle.MutableLiveData
import br.com.gabrielmarcos.githubmvvm.base.gateway.RxViewModel
import br.com.gabrielmarcos.githubmvvm.data.Event
import br.com.gabrielmarcos.githubmvvm.model.FavModel
import br.com.gabrielmarcos.githubmvvm.model.Gist
import javax.inject.Inject

open class GistViewModel @Inject constructor(
    private val gistRepository: GistRepository
) : RxViewModel() {

    var currentPage = 0
    var connectionAvailability: Boolean = true

    // Only For testes uuuh ugly :(
    internal var listResult: List<Gist> = emptyList()
    internal var favIdList: List<String> = emptyList()

    internal val showSnackbarMessage: MutableLiveData<Event<String>> = MutableLiveData()
    internal val showLoading: MutableLiveData<Event<Unit>> = MutableLiveData()
    internal val gistListViewState: MutableLiveData<List<Gist>> = MutableLiveData()
    internal val gistDetailViewState: MutableLiveData<Gist> = MutableLiveData()
    internal val resultError: MutableLiveData<Event<Unit>> = MutableLiveData()
    internal val resultSuccess: MutableLiveData<Event<Unit>> = MutableLiveData()

    fun getGistList() {
        runSingle<List<Gist>> {
            runBefore {
                showLoading.value = Event(Unit)
            }
            withSingle { gistRepository.getGistList(currentPage, connectionAvailability) }
            withSuccess(::handleGistListResult)
            withFailure(::handleError)
        }
    }

    private fun handleGistListResult(gist: List<Gist>) {
        getLocalFavoriteList()
        listResult = gist
    }

    fun getLocalFavoriteList() {
        runSingle<List<FavModel>> {
            withSingle { gistRepository.getSavedFavoriteGist() }
            withSuccess(::getOnlyFavId)
            withFailure(::handleError)
        }
    }

    private fun getOnlyFavId(favList: List<FavModel>) {
        val favIds = ArrayList<String>()
        favList.forEach { favIds.add(it.favId) }
        favIdList = favIds
        buildGistMapper()
    }

    private fun buildGistMapper() {
        handleListUpdate().let {
            gistListViewState.value = it
            saveLocalResponse(it)
        }
        resultSuccess.value = Event(Unit)
    }

    private fun handleListUpdate(): List<Gist> {
        return if (gistListViewState.value.isNullOrEmpty()) {
            GistMapper.map(listResult, favIdList)
        } else {
            GistMapper.map(
                gistListViewState.value!!
                    .plus(listResult)
                    .distinct(),
                favIdList
            )
        }
    }

    fun getGist(id: String) {
        runSingle<Gist> {
            withSingle {
                gistRepository.getGist(id, connectionAvailability)
            }
            withSuccess(::gistSuccess)
            withFailure(::handleError)
        }
    }

    private fun gistSuccess(gist: Gist) {
        gistDetailViewState.value = gist
        resultSuccess.value = Event(Unit)
    }

    private fun handleError(throwable: Throwable) {
        showSnackbarMessage.value = Event(throwable.message ?: "")
        resultError.value = Event(Unit)
    }

    fun saveLocalResponse(gist: List<Gist>) {
        runCompletable {
            withFunction {
                gistRepository.saveLocalGist(gist)
            }
        }
    }

    fun handleFavoriteState(gist: Gist) {
        takeIf { gist.starred }?.run {
            addGistFav(gist)
        } ?: deleteGistFav(gist.gistId)
    }

    private fun addGistFav(gist: Gist) {
        runCompletable {
            withFunction {
                gistRepository.setFavoriteGist(gist)
            }
        }
    }

    private fun deleteGistFav(gistId: String) {
        runCompletable {
            withFunction {
                gistRepository.deletFavoriteGistById(gistId)
            }
        }
    }

    fun updateGistList() {
        currentPage++
        getGistList()
    }

    fun filterGistOwner(ownerLogin: String?) {
        //TODO Searchview needed share viewmodel between activity and fragment
    }
}
