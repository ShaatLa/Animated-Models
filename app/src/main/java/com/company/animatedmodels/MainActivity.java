package com.company.animatedmodels;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

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
}

