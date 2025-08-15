package com.example.core.base;

import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

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
     * 获取当前 CompositeDisposable（可选）
     */
    protected CompositeDisposable getDisposables() {
        return disposables;
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
