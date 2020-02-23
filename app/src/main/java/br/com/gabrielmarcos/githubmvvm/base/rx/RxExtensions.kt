package br.com.gabrielmarcos.githubmvvm.base.rx

import io.reactivex.Completable
import io.reactivex.Single

@DslMarker
annotation class SingleSubscriberDslMarker

@DslMarker
annotation class CompletableSubscriberDslMarker

@SingleSubscriberDslMarker
class SingleSubscriberDSLBuilder {
    fun <P> build(function: SingleSubscriberDSL<P>.() -> Unit): SingleSubscriberDSL<P> =
        SingleSubscriberDSL<P>()
            .apply(function)
            .also { it.onBefore() }
}
@CompletableSubscriberDslMarker
class CompletableSubscriberDSLBuilder: CompletableSubscriberDsl() {
    fun build(function: CompletableSubscriberDsl.() -> Unit): CompletableSubscriberDsl =
            this.apply(function).also { it.onBefore() }
}

@CompletableSubscriberDslMarker
abstract class CompletableSubscriberDsl {
    var function: () -> Completable = { Completable.complete() }
    var onBefore: () -> Unit = {}
    var onComplete: () -> Unit = {}
    var onFailure: (error: Throwable) -> Unit = {}

    fun withFunction(function: () -> Completable) {
        this.function = function
    }

    fun withSuccess(onComplete: () -> Unit) {
        this.onComplete = onComplete
    }

    fun withFailure(onFailure: (error: Throwable) -> Unit) {
        this.onFailure = onFailure
    }

    fun runBefore(onBeforeArgument: () -> Unit) {
        onBefore = onBeforeArgument
    }

    fun execute(): Completable {
        return function()
    }
}

@SingleSubscriberDslMarker
class SingleSubscriberDSL<P> {
    var single: () -> Single<P> = { Single.never() }
    var onBefore: () -> Unit = {}
    var onSuccess: (result: P) -> Unit = {}
    var onFailure: (error: Throwable) -> Unit = {}

    fun withSingle(single: () -> Single<P>) {
        this.single = single
    }

    fun withSuccess(onSuccess: (result: P) -> Unit) {
        this.onSuccess = onSuccess
    }

    fun withFailure(onFailure: (error: Throwable) -> Unit) {
        this.onFailure = onFailure
    }

    fun runBefore(onBeforeArgument: () -> Unit) {
        onBefore = onBeforeArgument
    }

    // execute the var `single` only when it's properly subscribed
    fun defer(): Single<P> = Single.defer(single)
}