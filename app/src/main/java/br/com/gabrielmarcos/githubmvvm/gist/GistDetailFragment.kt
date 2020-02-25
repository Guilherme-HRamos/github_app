package br.com.gabrielmarcos.githubmvvm.gist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import br.com.gabrielmarcos.githubmvvm.R
import br.com.gabrielmarcos.githubmvvm.base.view.BaseFragment
import br.com.gabrielmarcos.githubmvvm.extensions.activityViewModelProvider
import br.com.gabrielmarcos.githubmvvm.extensions.hide
import br.com.gabrielmarcos.githubmvvm.model.Gist
import br.com.gabrielmarcos.githubmvvm.util.bindImageFromUrl
import kotlinx.android.synthetic.main.gist_detail_fragment.*

class GistDetailFragment : BaseFragment() {

    private val args: GistDetailFragmentArgs by navArgs()

    private lateinit var viewModel: GistViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.gist_detail_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpViewModel()
        setUpObservables()
    }

    private fun setUpViewModel() {
        viewModel = activityViewModelProvider(viewModelFactory)
        viewModel.getGist(args.gitsId ?: "")
    }

    private fun setUpObservables() {
        viewModel.observeGistDetailViewState(viewLifecycleOwner) {
            createSnackbarObserver(snackBarLiveData)
            onReceiveGist = {
                setUpView(it)
            }
        }
    }

    private fun setUpView(gist : Gist) {
        bindImageFromUrl(gistDetailImage, gist.owner.photo)
        gistDetailName.text = gist.owner.login
        gistDetailLoading.hide()
    }
}