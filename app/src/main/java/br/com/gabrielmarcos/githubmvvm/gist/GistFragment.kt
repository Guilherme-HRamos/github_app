package br.com.gabrielmarcos.githubmvvm.gist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.gabrielmarcos.githubmvvm.R
import br.com.gabrielmarcos.githubmvvm.base.view.BaseFragment
import br.com.gabrielmarcos.githubmvvm.data.Event
import br.com.gabrielmarcos.githubmvvm.data.EventObserver
import br.com.gabrielmarcos.githubmvvm.extensions.hide
import br.com.gabrielmarcos.githubmvvm.extensions.injectViewModel
import br.com.gabrielmarcos.githubmvvm.extensions.show
import br.com.gabrielmarcos.githubmvvm.extensions.showIf
import br.com.gabrielmarcos.githubmvvm.model.Gist
import br.com.gabrielmarcos.githubmvvm.util.InfiniteScrollListener
import br.com.gabrielmarcos.githubmvvm.util.InternetUtil
import br.com.gabrielmarcos.githubmvvm.util.NavigationCustom
import br.com.gabrielmarcos.githubmvvm.util.printer
import kotlinx.android.synthetic.main.gist_fragment.*

class GistFragment : BaseFragment() {

    private lateinit var viewModel: GistViewModel

    private val gistAdapter = GistAdapter(
        { gist -> onItemSelected(gist) },
        { gist -> onFavoriteItemSelected(gist) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.gist_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpViewModel()
        setUpObservables()
        setUpAdapter()
    }

    private fun setUpViewModel() {
        viewModel = injectViewModel(viewModelFactory)
        viewModel.connectionAvailability = InternetUtil.isInternetOn()
        viewModel.getGistList()
    }

    private fun setUpObservables() {

        viewModel.observeGistListViewState(viewLifecycleOwner) {
            createSnackbarObserver(snackBarLiveData)
            onLoading = {
                gistProgressBar.showIf(it)
            }
            onSuccess = {
                gistProgressBar.hide()
                gistRecyclerView.showIf(it)
                gistGroupError.showIf(!it)
            }
            onReceiveGistList = {
                gistAdapter.items = it
            }
        }
    }

    private fun setUpAdapter() {
        val linearLayout = LinearLayoutManager(requireContext())
        gistRecyclerView.apply {
            layoutManager = linearLayout
            adapter = gistAdapter
            addOnScrollListener(
                InfiniteScrollListener(
                    { updateList() }, linearLayout
                )
            )
        }
    }

    private fun updateList() = viewModel.updateGistList()

    private fun onItemSelected(item: Gist) {
        val directions = GistFragmentDirections.actionGistToGistDetail(item.gistId)
        NavigationCustom.navigateRight(this, directions)
    }

    private fun onFavoriteItemSelected(item: Gist) = viewModel.handleFavoriteState(item)
}
