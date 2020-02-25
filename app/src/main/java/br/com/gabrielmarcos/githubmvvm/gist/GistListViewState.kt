package br.com.gabrielmarcos.githubmvvm.gist

import androidx.lifecycle.MutableLiveData
import br.com.gabrielmarcos.githubmvvm.base.view.MainViewState
import br.com.gabrielmarcos.githubmvvm.model.Gist

class GistListViewState: MainViewState() {

    val gistListLiveData: MutableLiveData<List<Gist>> = MutableLiveData()

    var onReceiveGistList: (List<Gist>) -> Unit = {}

    fun getListValue(): List<Gist> {
        return gistListLiveData.value ?: emptyList()
    }
}