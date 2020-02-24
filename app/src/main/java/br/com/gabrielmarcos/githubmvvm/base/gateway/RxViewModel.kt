package br.com.gabrielmarcos.githubmvvm.base.gateway

import androidx.lifecycle.ViewModel
import br.com.gabrielmarcos.githubmvvm.base.rx.CompletableSubscriberCreator
import br.com.gabrielmarcos.githubmvvm.base.rx.SchedulersFacade
import br.com.gabrielmarcos.githubmvvm.base.rx.SingleSubscriberCreator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class RxViewModel : ViewModel() {
    private val disposables: CompositeDisposable = CompositeDisposable()

    fun runCompletable(completableSubscriber: CompletableSubscriberCreator.() -> Unit) {
        addDisposable {
            CompletableSubscriberCreator
                .create(completableSubscriber)
                .configureCompletable()
        }
    }

    fun <P> runSingle(singleSubscriber: SingleSubscriberCreator<P>.() -> Unit) {
        addDisposable {
            SingleSubscriberCreator
                .create(singleSubscriber)
                .configureSingle()
        }
    }

    private fun <P> SingleSubscriberCreator<P>.configureSingle(): Disposable {
        return defer()
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .subscribe({ dispatchSuccess(it) }, { dispatchFailure(it) })
    }

    private fun CompletableSubscriberCreator.configureCompletable(): Disposable {
        return execute()
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .subscribe({ dispatchComplete() }, { dispatchFailure(it) })
    }

    private fun addDisposable(disposableFunction: () -> Disposable) {
        disposables.add(disposableFunction())
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}