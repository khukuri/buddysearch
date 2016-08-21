package com.buddysearch.presentation.domain.interactor;

import com.buddysearch.presentation.domain.repository.Repository;

import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public abstract class UseCase<REQUEST_DATA, RESPONSE_DATA, REPOSITORY extends Repository> {

    final REPOSITORY repository;

    private final Scheduler threadScheduler;

    private final Scheduler postExecutionScheduler;

    private CompositeSubscription subscription = new CompositeSubscription();

    public UseCase(REPOSITORY repository, @Named("Thread") Scheduler threadScheduler, @Named("PostExecution") Scheduler postExecutionScheduler) {
        this.repository = repository;
        this.threadScheduler = threadScheduler;
        this.postExecutionScheduler = postExecutionScheduler;
    }

    protected abstract Observable<RESPONSE_DATA> buildObservable(REQUEST_DATA requestData);

    public void execute(REQUEST_DATA requestData, Subscriber<RESPONSE_DATA> useCaseSubscriber) {
        this.subscription.add(this.buildObservable(requestData)
                .doOnNext(new Action1<RESPONSE_DATA>() {
                    @Override
                    public void call(RESPONSE_DATA response_data) {
                        System.out.println(" CACHE CACHE CACHE CACHE");
                    }
                })
                .subscribeOn(threadScheduler)
                .observeOn(postExecutionScheduler)
                .subscribe(useCaseSubscriber));
    }

    public boolean isUnsubscribed() {
        return !subscription.hasSubscriptions();
    }

    public void unsubscribe() {
        if (!isUnsubscribed()) {
            subscription.clear();
        }
    }
}