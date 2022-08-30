package com.tajos.graedeviewer.animator;

import android.content.Context;
import android.view.ViewGroup;

import androidx.transition.Scene;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.transition.TransitionManager;

import com.tajos.graedeviewer.R;

public class SceneAnimator {

    protected final int mStartSceneId;
    protected final int mEndSceneId;
    protected final ViewGroup mSceneRoot;
    protected Scene startScene, endScene;

    private Transition transition;

    protected Context context;

    public SceneAnimator(final Context cn, final ViewGroup sceneRoot, final int startSceneId, final int endSceneId) {
        context = cn;
        mStartSceneId = startSceneId; mEndSceneId = endSceneId;
        mSceneRoot = sceneRoot;
        _initScenes();
    }

    private void _initScenes() {
        transition = TransitionInflater.from(context).inflateTransition(R.transition.transition_set);
        startScene = Scene.getSceneForLayout(mSceneRoot, mStartSceneId, context);
        endScene = Scene.getSceneForLayout(mSceneRoot, mEndSceneId, context);
    }

    public void animateToEndScene() {
        TransitionManager.go(endScene, transition);
    }

    public void animateToStartScene() {
        TransitionManager.go(startScene, transition);
    }

    protected Transition getTransition() {
        return transition;
    }
}
