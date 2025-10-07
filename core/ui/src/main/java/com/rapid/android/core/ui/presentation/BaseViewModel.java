package com.rapid.android.core.ui.presentation;

import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


public abstract class BaseViewModel extends ViewModel {
    private volatile CompositeDisposable disposables;

    /**
     * 添加一个 Disposable（自动管理生命周期）
     */
    protected void autoDispose(Disposable disposable) {
        if (disposable == null) return;
        if (disposables == null) {
            synchronized (this) {
                if (disposables == null) {
                    disposables = new CompositeDisposable();
                }
            }
        }
        disposables.add(disposable);
    }

    /**
     * 批量添加多个 Disposable（自动管理生命周期）
     */
    protected void autoDispose(Disposable... disposables) {
        if (disposables == null || disposables.length == 0) return;
        for (Disposable disposable : disposables) {
            autoDispose(disposable);
        }
    }

    /**
     * 向外暴露的订阅管理入口，便于辅助类统一托管 Disposable。
     */
    public void trackDisposable(Disposable disposable) {
        autoDispose(disposable);
    }

    /**
     * 移除指定的 Disposable
     */
    protected void removeDisposable(Disposable disposable) {
        if (disposable != null && disposables != null) {
            disposables.remove(disposable);
        }
    }

    /**
     * 清除所有已添加的 Disposable
     */
    protected void clearDisposables() {
        if (disposables != null) {
            disposables.clear();
        }
    }

    @Override
    protected void onCleared() {
        if (disposables != null) {
            disposables.clear(); // 关键：自动释放所有订阅
            disposables = null;
        }
        super.onCleared();
    }
}
