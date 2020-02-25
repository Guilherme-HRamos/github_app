package br.com.gabrielmarcos.githubmvvm.base.gateway

import androidx.lifecycle.ViewModel
import br.com.gabrielmarcos.githubmvvm.base.rx.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class RxViewModel : ViewModel() {
    private val disposables: CompositeDisposable = CompositeDisposable()

    fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun disposableRxThread(
        completable: Completable
    ) {
        addToDisposable(
            completable
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .subscribe({ Timber.i("On Success: RxViewModel") },
                    { Timber.i("On Error: RxViewModel") })
        )
    }

    fun <P> disposableRxThread(
        single: Single<P>,
        subscribeSuccess: (result: P) -> Unit,
        subscribeError: (t: Throwable) -> Unit
    ) {
        addToDisposable(
            single
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnError { subscribeError(it) }
                .retryWhen { it.retryWithDelay(3, 2000) }
                .subscribe(
                    { success -> subscribeSuccess(success) },
                    { error -> subscribeError(error) }
                )
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun <T> Flowable<T>.retryWithDelay(maxRetries: Int, retryDelayMillis: Int): Flowable<T> {
        var retryCount = 0
        return retryWhen { thObservable ->
            thObservable.flatMap { throwable ->
                if (++retryCount < maxRetries) {
                    Flowable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS)
                } else {
                    Flowable.error(throwable)
                }
            }
        }
    }
}