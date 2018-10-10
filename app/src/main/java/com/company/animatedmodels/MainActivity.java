package com.company.animatedmodels;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Debug";

    ArFragment arFragment;
    ModelRenderable modelRenderable;
    AnchorNode anchorNode;
    TransformableNode islandNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        ImageView cancel = (ImageView) findViewById(R.id.cancelButton);

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
                    anchorNode = new AnchorNode(anchor);

                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    islandNode = new TransformableNode(arFragment.getTransformationSystem());
                    islandNode.setParent(anchorNode);
                    islandNode.setRenderable(modelRenderable);
                }

        );

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                anchorNode.removeChild(arFragment.getArSceneView().getScene().findByName(islandNode.getName()));
            }
        });
    }
}
