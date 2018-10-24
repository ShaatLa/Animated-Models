package com.company.animatedmodels;

import android.animation.ObjectAnimator;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Debug";

    ArFragment arFragment;
    ModelRenderable islandRenderable;
    ModelRenderable cloudsRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView cancel = findViewById(R.id.cancelButton);
        cancel.setVisibility(ImageView.GONE);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        ModelRenderable.builder()
                .setSource(this, Uri.parse("mountains.sfb"))
                .build()
                .thenAccept(renderable -> islandRenderable = renderable)
                .exceptionally(throwable -> {
                    Log.d(TAG, "Couldn't create renderable...");
                    throwable.printStackTrace();
                    return null;
                });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("clouds.sfb"))
                .build()
                .thenAccept(renderable -> cloudsRenderable = renderable)
                .exceptionally(throwable -> {
                    Log.d(TAG, "Couldn't create renderable...");
                    return null;
                });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (islandRenderable == null || cloudsRenderable == null) {
                        Log.d(TAG, "Models is not ready");
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    TransformableNode islandNode = new TransformableNode(arFragment.getTransformationSystem());
                    TransformableNode cloudsNode = new TransformableNode(arFragment.getTransformationSystem());
                    islandNode.setRenderable(islandRenderable);
                    cloudsNode.setRenderable(cloudsRenderable);
                    cloudsNode.setLocalPosition(new Vector3(0, 0.7f, 0));
                    islandNode.addChild(cloudsNode);
                    anchorNode.addChild(islandNode);

                    showCancelButton(cancel, islandNode, anchorNode);

                    islandNode.setOnTapListener((hitTestResult, motionEvent1) -> showCancelButton(cancel, islandNode, anchorNode));

                    ObjectAnimator levitateAnimator = createLevitateAnimator(islandNode);
                    ObjectAnimator roundAnimator = createRoundAnimator(cloudsNode);
                    roundAnimator.start();
                    levitateAnimator.start();
                    arFragment.getPlaneDiscoveryController().hide();
                }
        );
    }

    public void showCancelButton(ImageView button, TransformableNode nodeToDelete, AnchorNode parentNode) {
        button.setVisibility(ImageView.VISIBLE);
        button.setOnClickListener(v -> {
            if (arFragment.getArSceneView().getScene().getChildren().contains(nodeToDelete)) {
                arFragment.getArSceneView().getScene().removeChild(parentNode);
                button.setVisibility(ImageView.GONE);
            }
        });
    }

    private static ObjectAnimator createLevitateAnimator(TransformableNode targetNode) {
        ObjectAnimator levitateAnimation = new ObjectAnimator();
        levitateAnimation.setObjectValues(new Vector3(0f, 0f, 0f), new Vector3(0f, 0.015f, 0f));
        levitateAnimation.setEvaluator(new Vector3Evaluator());
        levitateAnimation.setPropertyName("localPosition");
        levitateAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        levitateAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        levitateAnimation.setInterpolator(new LinearInterpolator());
        levitateAnimation.setAutoCancel(true);
        levitateAnimation.setDuration(3000);
        levitateAnimation.setTarget(targetNode);
        return levitateAnimation;
    }

    private static ObjectAnimator createRoundAnimator(TransformableNode targetNode) {
        ObjectAnimator roundAnimation = new ObjectAnimator();

        Vector3 horizontalVector = new Vector3(0, 1, 0);
        Quaternion quaternion1 = Quaternion.axisAngle(horizontalVector, 0);
        Quaternion quaternion2 = Quaternion.axisAngle(horizontalVector, 120);
        Quaternion quaternion3 = Quaternion.axisAngle(horizontalVector, 240);
        Quaternion quaternion4 = Quaternion.axisAngle(horizontalVector, 360);

        roundAnimation.setObjectValues(quaternion1, quaternion2, quaternion3, quaternion4);
        roundAnimation.setEvaluator(new QuaternionEvaluator());
        roundAnimation.setPropertyName("localRotation");
        roundAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        roundAnimation.setInterpolator(new LinearInterpolator());
        roundAnimation.setAutoCancel(true);
        roundAnimation.setDuration(32000);
        roundAnimation.setTarget(targetNode);

        return roundAnimation;
    }
}

