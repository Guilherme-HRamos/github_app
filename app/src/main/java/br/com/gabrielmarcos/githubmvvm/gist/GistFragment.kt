package br.com.gabrielmarcos.githubmvvm.gist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.gabrielmarcos.githubmvvm.R
import br.com.gabrielmarcos.githubmvvm.base.view.BaseFragment
import br.com.gabrielmarcos.githubmvvm.data.EventObserver
import br.com.gabrielmarcos.githubmvvm.extensions.activityViewModelProvider
import br.com.gabrielmarcos.githubmvvm.extensions.hide
import br.com.gabrielmarcos.githubmvvm.extensions.show
import br.com.gabrielmarcos.githubmvvm.model.Gist
import br.com.gabrielmarcos.githubmvvm.util.InfiniteScrollListener
import br.com.gabrielmarcos.githubmvvm.util.InternetUtil
import br.com.gabrielmarcos.githubmvvm.util.NavigationCustom
import kotlinx.android.synthetic.main.gist_fragment.*

class GistFragment : BaseFragment() {

    private lateinit var viewModel: GistViewModel

    private var neededUpdate = false

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
        setUpNetworkObserve()
        setUpSnackbar(this.view!!, viewModel.showSnackbarMessage)
    }

    private fun setUpViewModel() {
        viewModel = activityViewModelProvider(viewModelFactory)
        viewModel.connectionAvailability = InternetUtil.isInternetOn()
        viewModel.getGistList()
    }

    private fun setUpNetworkObserve() {
        InternetUtil.observe(this, Observer { hasNetwork ->
            viewModel.connectionAvailability = hasNetwork
            checkNetworkConnection(hasNetwork)
        })
    }

    private fun checkNetworkConnection(hasNetwork: Boolean) {
        takeIf { !hasNetwork }?.run {
            neededUpdate = true
        } ?: checkRefreshList()
    }

    private fun checkRefreshList() {
        takeIf { neededUpdate }?.run {
            viewModel.getGistList()
            neededUpdate = false
        }
    }

    private fun setUpObservables() {
        viewModel.gistListViewState.observe(viewLifecycleOwner, Observer { gistAdapter.items = it })

        viewModel.showLoading.observe(viewLifecycleOwner, EventObserver { gistProgressBar.show() })

        viewModel.resultSuccess.observe(viewLifecycleOwner, EventObserver { showSuccessLayout() })

        viewModel.emptyResult.observe(viewLifecycleOwner, EventObserver { setUpEmptyLayout() })

        viewModel.resultError.observe(viewLifecycleOwner, EventObserver { setUpErrorLayout() })
    }

    private fun showSuccessLayout() {
        gistRecyclerView.show()
        gistGroupError.hide()
        gistProgressBar.hide()
    }

    private fun setUpEmptyLayout() {
        gistErrorIcon.setImageResource(R.drawable.ic_not_found)
        gistErrorTitle.text = getString(R.string.gist_empty_error_title)
        gistErrorMessage.text = getString(R.string.gist_empty_error_description)
        showHandleProblemLayout()
    }

    private fun setUpErrorLayout() {
        gistErrorIcon.setImageResource(R.drawable.ic_problem)
        gistErrorTitle.text = getString(R.string.gist_error_title)
        gistErrorMessage.text = getString(R.string.gist_error_description)
        showHandleProblemLayout()
    }

    private fun showHandleProblemLayout() {
        gistGroupError.show()
        gistProgressBar.hide()
        gistRecyclerView.hide()
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
