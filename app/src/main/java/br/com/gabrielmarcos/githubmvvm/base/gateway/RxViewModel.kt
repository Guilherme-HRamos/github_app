package br.com.gabrielmarcos.githubmvvm.base.gateway

import androidx.lifecycle.ViewModel
import br.com.gabrielmarcos.githubmvvm.base.rx.CompletableSubscriberDSLBuilder
import br.com.gabrielmarcos.githubmvvm.base.rx.CompletableSubscriberDsl
import br.com.gabrielmarcos.githubmvvm.base.rx.SchedulersFacade
import br.com.gabrielmarcos.githubmvvm.base.rx.SingleSubscriberDSL
import br.com.gabrielmarcos.githubmvvm.base.rx.SingleSubscriberDSLBuilder
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class RxViewModel : ViewModel() {
    private val disposables: CompositeDisposable = CompositeDisposable()

    fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    private fun addDisposable(disposableFunction: () -> Disposable) {
        disposables.add(disposableFunction())
    }

    fun disposableRxThread(
        completable: Completable
    ) {
        addToDisposable(
            completable
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .subscribe({ println("On Success: RxViewModel") },
                    { println("On Error: RxViewModel") })
        )
    }

    fun runCompletable(completableSubscriber: CompletableSubscriberDsl.() -> Unit) {
        addDisposable {
            CompletableSubscriberDSLBuilder()
                .build(completableSubscriber)
                .configureCompletable()
        }
    }

    fun <P> runSingle(singleSubscriber: SingleSubscriberDSL<P>.() -> Unit) {
        addDisposable { buildSingle(singleSubscriber) }
    }

    private fun <P> buildSingle(singleSubscriber: SingleSubscriberDSL<P>.() -> Unit): Disposable {
        return SingleSubscriberDSLBuilder()
            .build(singleSubscriber)
            .configureSingle()
    }

    private fun <P> SingleSubscriberDSL<P>.configureSingle(): Disposable {
        return defer()
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .subscribe({ onSuccess(it) }, { onFailure(it) })
    }

    private fun CompletableSubscriberDsl.configureCompletable(): Disposable {
        return execute()
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .subscribe({ onComplete() }, { onFailure(it) })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}