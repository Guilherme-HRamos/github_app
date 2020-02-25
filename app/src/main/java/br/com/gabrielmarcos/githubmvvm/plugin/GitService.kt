package br.com.gabrielmarcos.githubmvvm.plugin

import br.com.gabrielmarcos.githubmvvm.model.Gist
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitService {

    @GET("/gists/public")
    fun getGists(@Query("page") page: Int): Single<List<Gist>>

    @GET("/gists/{id}")
    fun getGist(@Path("id") id: String): Single<Gist>
}