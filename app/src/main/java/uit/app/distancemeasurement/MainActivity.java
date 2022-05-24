package uit.app.distancemeasurement;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;

import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ArFragment arFragment;
    private ModelRenderable andyRenderable;

    List<AnchorNode> anchorNodes = new ArrayList<>();
    ArrayList<Anchor> arrAnchors = new ArrayList<>();
    private TextView text;
    private Button btnClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        maybeEnableArButton();

        btnClear = findViewById(R.id.Clear);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        text = findViewById(R.id.text);
        MaterialFactory
                .makeTransparentWithColor(this, new Color(0.0f,0.0f,1.0f,0.5f))
                .thenAccept(material -> {
                    andyRenderable = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material);
                }).exceptionally(
                throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.i(TAG, "modelRendable: null ");
                    return null;
                }
        );

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAll();
            }
        });



        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
//                    Log.i(TAG, "arFragment: clicked");
//                    if (andyRenderable == null) {
//                        Log.i(TAG, "onCreate: null andyRendable");
//                        return;
//                    }
//                    myhit = hitResult;
//
//                    // Create the Anchor.
//                    Anchor anchor = hitResult.createAnchor();
//
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                    if (anchor1 == null) {
//                        anchor1 = anchor;
//                        Log.i(TAG, "onCreate: anchor1 added");
//
//                    }
//                    if (anchor2 == null) {
//                        anchor2 = anchor;
//                        Log.i(TAG, "onCreate: anchor2 added");
//                        distance = getMetersBetweenAnchors(anchor1, anchor2);
//                        Log.i(TAG, "onCreate:" + distance);
//                        text.setText("Distance: " + distance);
//                    }
//                    else {
//                        text.setText("");
//                        emptyAnchors();
//                        anchor1 = anchor;
//                    }
//
//                    myanchornode = anchorNode;
//                    anchorNodes.add(anchorNode);
//
//                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();
//                    andy.getScaleController().setEnabled(false);
                    if (andyRenderable == null) {
                        Log.i(TAG, "onCreate: null andyRendable");
                        return;
                    }
                    if (anchorNodes.size() < 2 ){
                        Anchor anchor = hitResult.createAnchor();
                        arrAnchors.add(anchor);

                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        anchorNodes.add(anchorNode);

                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                        andy.setParent(anchorNode);
                        andy.setRenderable(andyRenderable);
                        andy.select();
                        andy.getScaleController().setEnabled(false);
                        andy.getRotationController().setEnabled(false);

                        arFragment.getArSceneView().getScene().addChild(anchorNode);
                        text.setText("");
                    }

                    else {
                        Log.i(TAG, "onCreate: size:" + anchorNodes.size());
                    }
                    arFragment.getArSceneView().getScene().addOnUpdateListener(this);
                });


    }



    private void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            Log.i(TAG, "maybeEnableArButton: true");
        } else { // The device is unsupported or unknown.
            Log.i(TAG, "maybeEnableArButton: false");
            Toast.makeText(this, "This device is not support for this app.", Toast.LENGTH_LONG).show();
        }
    }

    // Verify that ARCore is installed and using the current version.


    private void measureDistanceOf2Points(){
        if (anchorNodes.size() == 2) {
            float distanceMeter = calculateDistance(anchorNodes.get(0).getWorldPosition(),anchorNodes.get(1).getWorldPosition());
            text.setText("distance:" + distanceMeter);
        }
    }

    private float calculateDistance(Vector3 objectPose0, Vector3 objectPose1){
       return calculateDistance(
               objectPose0.x - objectPose1.x,
               objectPose0.y-objectPose1.y,
               objectPose0.z-objectPose1.z);
    }
    private float calculateDistance(float x, float y, float z){
        return (float) Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
    }

    @Override
    public void onUpdate(FrameTime frameTime) {

        measureDistanceOf2Points();
    }

    private void clearAll(){
        arrAnchors.clear();
        for(int i = 0; i < anchorNodes.size();i++){
            AnchorNode a = anchorNodes.get(i);
            arFragment.getArSceneView().getScene().removeChild(a);
            a.setEnabled(false);
            a.getAnchor().detach();
            a.setParent(null);
        }
        anchorNodes.clear();
        text.setText("");
    }
}