package br.com.gabrielmarcos.githubmvvm.gist

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import br.com.gabrielmarcos.githubmvvm.base.gateway.RxViewModel
import br.com.gabrielmarcos.githubmvvm.base.view.MainViewState
import br.com.gabrielmarcos.githubmvvm.data.Event
import br.com.gabrielmarcos.githubmvvm.data.EventObserver
import br.com.gabrielmarcos.githubmvvm.model.FavModel
import br.com.gabrielmarcos.githubmvvm.model.Gist
import br.com.gabrielmarcos.githubmvvm.util.emptyString
import br.com.gabrielmarcos.githubmvvm.util.then
import javax.inject.Inject

open class GistViewModel @Inject constructor(
    private val gistRepository: GistRepository
) : RxViewModel() {

    var currentPage = 0
    var connectionAvailability: Boolean = true

    // Only For testes uuuh ugly :(
    var listResult: List<Gist> = emptyList()
    internal var favIdList: List<String> = emptyList()

    var mainViewState = MainViewState()
    var gistListViewState = GistListViewState()
    val gistDetailViewState = GistDetailViewState()

    fun observeGistListViewState(
        owner: LifecycleOwner,
        dslViewState: GistListViewState.() -> Unit
    ) {
        mainViewState = gistListViewState
            .also(dslViewState)
            .apply {
                loadingLiveData.observe(owner, EventObserver {
                    onLoading(it)
                })
                successLiveData.observe(owner, EventObserver {
                    onSuccess(it)
                })
                snackBarLiveData.observe(owner, EventObserver {
                    onShowMessage(it)
                })
                gistListLiveData.observe(owner, Observer {
                    onReceiveGistList(it)
                })
            }
    }

    fun observeGistDetailViewState(
        owner: LifecycleOwner,
        dslViewState: GistDetailViewState.() -> Unit
    ) {
        mainViewState = gistDetailViewState
            .also(dslViewState)
            .apply {
                loadingLiveData.observe(owner, EventObserver {
                    onLoading(it)
                })
                successLiveData.observe(owner, EventObserver {
                    onSuccess(it)
                })
                snackBarLiveData.observe(owner, EventObserver {
                    onShowMessage(it)
                })
                gistLiveData.observe(owner, Observer {
                    onReceiveGist(it)
                })
            }
    }

    fun getGistList() {
        runSingle<List<Gist>> {
            runBefore { updateLoadingState(true) }
            withSingle { gistRepository.getGistList(currentPage, connectionAvailability) }
            onSuccess(::handleGistListResult)
            onFailure(::handleError)
        }
    }

    fun getGist(id: String) {
        runSingle<Gist> {
            withSingle { gistRepository.getGist(id, connectionAvailability) }
            onSuccess(::gistSuccess)
            onFailure(::handleError)
        }
    }

    fun getLocalFavoriteList() {
        runSingle<List<FavModel>> {
            withSingle { gistRepository.getSavedFavoriteGist() }
            onSuccess(::getOnlyFavId)
            onFailure(::handleError)
        }
    }

    fun saveLocalResponse(gist: List<Gist>) {
        runCompletable {
            withFunction { gistRepository.saveLocalGist(gist) }
        }
    }

    private fun handleGistListResult(gist: List<Gist>) {
        getLocalFavoriteList()
        listResult = gist
    }

    private fun getOnlyFavId(favList: List<FavModel>) {
        favIdList = favList.mapStarredItems()
        buildGistMapper()
    }

    private fun List<FavModel>.mapStarredItems(): List<String> {
        return map { it.favId }.toList()
    }

    private fun filterGistList(): List<Gist> {
        return when {
            requestGistListValue().isNullOrEmpty() -> listResult
            else -> requestGistListValue().plus(listResult).distinct()
        }
    }

    private fun buildGistMapper() {
        GistMapper.map(filterGistList(), favIdList)
            .let {
                updateListState(it)
                saveLocalResponse(it)
                updateSuccessState(true)
            }
    }

    private fun gistSuccess(gist: Gist) {
        updateGistState(gist)
        updateSuccessState(true)
    }

    private fun handleError(throwable: Throwable) {
        updateSuccessState(false)
        updateSnackBarState(throwable.message ?: emptyString())
    }

    private fun addGistFav(gist: Gist) {
        runCompletable {
            withFunction { gistRepository.setFavoriteGist(gist) }
        }
    }

    private fun deleteGistFav(gistId: String) {
        runCompletable {
            withFunction { gistRepository.deletFavoriteGistById(gistId) }
        }
    }

    fun handleFavoriteState(gist: Gist) {
        gist.starred.then { addGistFav(gist) }
            ?: deleteGistFav(gist.gistId)
    }

    fun updateGistList() {
        currentPage++
        getGistList()
    }

    fun filterGistOwner(ownerLogin: String?) {
        //TODO Searchview needed share viewmodel between activity and fragment
    }

    private fun updateSuccessState(isSuccess: Boolean) {
        mainViewState.successLiveData.value = Event(isSuccess)
    }

    private fun updateLoadingState(showLoading: Boolean) {
        mainViewState.loadingLiveData.value = Event(showLoading)
    }

    private fun updateSnackBarState(string: String) {
        mainViewState.snackBarLiveData.value = Event(string)
    }

    private fun updateListState(data: List<Gist>) {
        gistListViewState.gistListLiveData.value = data
    }

    private fun updateGistState(data: Gist) {
        gistDetailViewState.gistLiveData.value = data
    }

    private fun requestGistListValue(): List<Gist> {
        return gistListViewState.getListValue()
    }
}
