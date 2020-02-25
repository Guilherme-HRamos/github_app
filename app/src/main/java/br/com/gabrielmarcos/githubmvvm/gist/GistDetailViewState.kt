package br.com.gabrielmarcos.githubmvvm.gist

import androidx.lifecycle.MutableLiveData
import br.com.gabrielmarcos.githubmvvm.base.view.MainViewState
import br.com.gabrielmarcos.githubmvvm.model.Gist

class GistDetailViewState: MainViewState() {

    val gistLiveData: MutableLiveData<Gist> = MutableLiveData()

    var onReceiveGist: (Gist) -> Unit = {}

}