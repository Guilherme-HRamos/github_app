package br.com.gabrielmarcos.githubmvvm.base.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import br.com.gabrielmarcos.githubmvvm.R
import br.com.gabrielmarcos.githubmvvm.extensions.viewModelProvider
import br.com.gabrielmarcos.githubmvvm.gist.GistViewModel
import br.com.gabrielmarcos.githubmvvm.util.InternetUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : BaseActivity() {

    private lateinit var gistViewModel: GistViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        checkNetworkConnection()
        setUpToolbar()
        setUpViewModel()
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left_outline_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setUpViewModel() {
        gistViewModel = viewModelProvider(viewModelFactory)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(quest: String?): Boolean {
                // gistViewModel.checkOwnerFilter(quest)
                setUpSnakMessage(R.string.gist_feature_in_progress, R.color.orange_warning)
                return true
            }
        })

        return true
    }

    private fun checkNetworkConnection() {
        InternetUtil.observe(this, Observer { status ->
            takeIf { !status }?.run {
                setUpSnakMessage(R.string.error_connection_unavailable, R.color.purple_attention)
            } ?: setUpSnakMessage(R.string.error_connection_available, R.color.green_success)
        })
    }

    private fun setUpSnakMessage(@StringRes message: Int, @DrawableRes color: Int) {
        val snackBar = Snackbar.make(activityContent, message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundResource(color)
        snackBar.show()
    }
}
