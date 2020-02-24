package br.com.gabrielmarcos.githubmvvm.base.rx

import io.reactivex.Completable
import io.reactivex.Single

abstract class CompletableSubscriber {
    protected var function: () -> Completable = { Completable.complete() }
    protected var before: () -> Unit = {}
    protected var complete: () -> Unit = {}
    protected var failure: (error: Throwable) -> Unit = {}
}

class CompletableSubscriberCreator: CompletableSubscriber() {

    companion object Builder {
        fun create(creator: CompletableSubscriberCreator.() -> Unit): CompletableSubscriberCreator {
            return CompletableSubscriberCreator()
                .apply(creator)
                .also { it.before() }
        }
    }

    fun dispatchComplete() = complete()

    fun dispatchFailure(error: Throwable) = failure(error)

    fun withFunction(function: () -> Completable) {
        this.function = function
    }

    fun onComplete(onComplete: () -> Unit) {
        this.complete = onComplete
    }

    fun onFailure(onFailure: (error: Throwable) -> Unit) {
        this.failure = onFailure
    }

    fun runBefore(onBeforeArgument: () -> Unit) {
        before = onBeforeArgument
    }

    fun execute(): Completable {
        return function()
    }
}

abstract class SingleSubscriber<P> {
    protected var function: () -> Single<P> = { Single.never() }
    protected var before: () -> Unit = {}
    protected var success: (result: P) -> Unit = {}
    protected var failure: (error: Throwable) -> Unit = {}
}

class SingleSubscriberCreator<P>: SingleSubscriber<P>() {

    companion object Builder {
        fun <P> create(creator: SingleSubscriberCreator<P>.() -> Unit): SingleSubscriberCreator<P> {
            return SingleSubscriberCreator<P>()
                .apply(creator)
                .also { it.before() }
        }
    }

    fun dispatchSuccess(result: P) = success(result)

    fun dispatchFailure(error: Throwable) = failure(error)

    fun withSingle(function: () -> Single<P>) {
        this.function = function
    }

    fun onSuccess(onSuccess: (result: P) -> Unit) {
        this.success = onSuccess
    }

    fun onFailure(onFailure: (error: Throwable) -> Unit) {
        this.failure = onFailure
    }

    fun runBefore(onBeforeArgument: () -> Unit) {
        before = onBeforeArgument
    }

    /**
     * Execute the var `single` only when it's properly subscribed
     * Example https://blog.mindorks.com/understanding-rxjava-defer-operator
     */
    fun defer(): Single<P> = Single.defer(function)
}