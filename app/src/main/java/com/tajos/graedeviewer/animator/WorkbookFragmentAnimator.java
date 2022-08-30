package com.tajos.graedeviewer.animator;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.transition.Transition;

public class WorkbookFragmentAnimator extends SceneAnimator {

    private Transition.TransitionListener transitionListener;

    private boolean isFromStartScene = false;

    private OnAnimatingListener listener;
    public interface OnAnimatingListener {
        void onStartedAnimating();
        void onFinishedAnimating(boolean fromStartScene);
    }

    public WorkbookFragmentAnimator(Context cn, ViewGroup sceneRoot, int startSceneId, int endSceneId) {
        super(cn, sceneRoot, startSceneId, endSceneId);
        _initListener();

        getTransition().addListener(transitionListener);
    }

    private void _initListener() {
        transitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                listener.onStartedAnimating();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                if (listener != null) {
                    listener.onFinishedAnimating(isFromStartScene = !isFromStartScene);
                }
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {}

            @Override
            public void onTransitionPause(@NonNull Transition transition) {}

            @Override
            public void onTransitionResume(@NonNull Transition transition) {}
        };
    }

    public OnAnimatingListener getListener() {
        return listener;
    }

    public void setListener(OnAnimatingListener listener) {
        this.listener = listener;
    }
}
