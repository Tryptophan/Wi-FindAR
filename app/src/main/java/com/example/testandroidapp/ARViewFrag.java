package com.example.testandroidapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ARViewFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ARViewFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ARViewFrag extends ArFragment {


    private ModelRenderable andyRenderable; //from the ARCore location example.
    private ArSceneView arSceneView;
    private ViewRenderable arViewLayoutRenderable;
    private boolean installRequested;
    private boolean hasFinishedLoading = false;
    private LocationScene locationScene; //use from location scene

    private OnFragmentInteractionListener mListener;


    public ARViewFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ARViewFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static ARViewFrag newInstance(String param1, String param2) {
        ARViewFrag fragment = new ARViewFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public void onCreate(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.fragment_arview, container, false);

        arSceneView = view.findViewById(R.id.ar_scene_view);

        // Build a renderable from a 2D View.
        CompletableFuture<ViewRenderable> arFragLayout =
                ViewRenderable.builder()
                        .setView(this.getActivity(), R.layout.fragment_arview)
                        .build();

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        CompletableFuture<ModelRenderable> andy = ModelRenderable.builder()
                                                    .setSource(this.getActivity(), R.raw.andy)
                                                    .build();
        CompletableFuture.allOf(andy)
                .handle(
                        (notUsed, throwable) ->
                        {
                            if (throwable != null) {
                                DemoUtils.displayError(getActivity(),
                                        "Unable to load renderables", throwable);
                                return null;
                            }

                            try {
                                arViewLayoutRenderable = arFragLayout.get();
                                andyRenderable = andy.get();

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(getActivity(),
                                        "Unable to load renderables", ex);
                            }
                            return null;
                        });

        arSceneView.getScene()
                        .addOnUpdateListener(
                        frameTime -> {
            if (!hasFinishedLoading) {
                return;
            }

            if (locationScene == null) {
                // If our locationScene object hasn't been setup yet, this is a good time to do it
                // We know that here, the AR components have been initiated.
                locationScene = new LocationScene(getActivity(),
                                                    getActivity(),
                                                    arSceneView);

                // Now lets create our location markers.
                // First, a layout
                LocationMarker layoutLocationMarker = new LocationMarker(
                        -4.849509,
                        42.814603,
                        getFragView()
                );

                // An example "onRender" event, called every frame
                // Updates the layout with the markers distance
                layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
                    @Override
                    public void render(LocationNode node) {
                        View eView = arViewLayoutRenderable.getView();
                        TextView distanceTextView = eView.findViewById(R.id.ar_scene_view);
                        distanceTextView.setText(node.getDistance() + "M");
                    }
                });
                // Adding the marker
                locationScene.mLocationMarkers.add(layoutLocationMarker);

                // Adding a simple location marker of a 3D model
                locationScene.mLocationMarkers.add(
                        new LocationMarker(
                                -0.119677,
                                51.478494,
                                getAndy()));
            }

            Frame frame = arSceneView.getArFrame();
            if (frame == null) {
                return;
            }

            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                return;
            }

            if (locationScene != null) {
                locationScene.processFrame(frame);
            }

        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arview, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public Node getAndy() {
        Node base = new Node();
        base.setRenderable(andyRenderable);
        Context c = getActivity();

        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    @Override
    public void onResume() {

        if(arSceneView != null) {


        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(getActivity(), installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(getActivity());
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(getActivity(), e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(getActivity(), "Unable to get camera", ex);
            return;
        }

        if (arSceneView.getSession() != null) {
            //showLoadingMessage();
        }
    }

    }

    private Node getFragView() {
        Node base = new Node();
        base.setRenderable(arViewLayoutRenderable);
        Context c = getActivity();
        // Add  listeners etc here
        View eView = arViewLayoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(
                    c, "Location marker touched.", Toast.LENGTH_LONG)
                    .show();
            return false;
        });

        return base;
    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

}
