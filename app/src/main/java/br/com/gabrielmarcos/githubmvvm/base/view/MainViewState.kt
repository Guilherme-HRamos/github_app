package br.com.gabrielmarcos.githubmvvm.base.view

import androidx.lifecycle.MutableLiveData
import br.com.gabrielmarcos.githubmvvm.data.Event

open class MainViewState {
    val loadingLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val successLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val snackBarLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    var onLoading: (Boolean) -> Unit = {}
    var onSuccess: (Boolean) -> Unit = {}
    var onShowMessage: (String) -> Unit = {}
}