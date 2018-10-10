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
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Debug";

    ArFragment arFragment;
    ModelRenderable modelRenderable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView cancel = (ImageView) findViewById(R.id.cancelButton);
        cancel.setVisibility(ImageView.GONE);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        ModelRenderable.builder()
                .setSource(this, Uri.parse("island.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Log.d(TAG, "Couldn't create renderable...");
                    return null;
                });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (modelRenderable == null) {
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    TransformableNode islandNode = new TransformableNode(arFragment.getTransformationSystem());
                    islandNode.setRenderable(modelRenderable);
                    anchorNode.addChild(islandNode);

                    showCancelButton(cancel, islandNode, anchorNode);

                    islandNode.setOnTapListener(new Node.OnTapListener() {
                        @Override
                        public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                            showCancelButton(cancel, islandNode, anchorNode);
                        }
                    });

                    ObjectAnimator animator = createAnimator();
                    animator.setTarget(islandNode);
                    animator.start();
                }
        );
    }

    public void showCancelButton(ImageView button, TransformableNode nodeToDelete, AnchorNode parentNode)
    {
        button.setVisibility(ImageView.VISIBLE);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                arFragment.getArSceneView().getScene().getChildren().contains(nodeToDelete);
                arFragment.getArSceneView().getScene().removeChild(parentNode);
                button.setVisibility(ImageView.GONE);
            }
        });
    }

    private static ObjectAnimator createAnimator() {
        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0);
        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120);
        Quaternion orientation3 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240);
        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360);

        ObjectAnimator roundAnimation = new ObjectAnimator();
        roundAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4);

        roundAnimation.setPropertyName("localRotation");

        roundAnimation.setEvaluator(new QuaternionEvaluator());

        roundAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        roundAnimation.setRepeatMode(ObjectAnimator.RESTART);
        roundAnimation.setInterpolator(new LinearInterpolator());
        roundAnimation.setAutoCancel(true);
        roundAnimation.setDuration(30000);

        return roundAnimation;
    }
}

