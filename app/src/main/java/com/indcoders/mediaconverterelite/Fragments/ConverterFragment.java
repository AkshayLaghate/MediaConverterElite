package com.indcoders.mediaconverterelite.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.indcoders.mediaconverterelite.R;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConverterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConverterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConverterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 6;
    File file;
    CircularProgressButton cpb, bConvert;
    TextView tvPath;
    Spinner spinner;
    FFmpeg ffmpeg;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private String inputPath, outPutName, outPutFormat;

    public ConverterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConverterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConverterFragment newInstance(String param1, String param2) {
        ConverterFragment fragment = new ConverterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        ffmpeg = FFmpeg.getInstance(getActivity());
        loadFFMpeg();

    }

    private void loadFFMpeg() {

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getActivity(), "FFMpeg Failed", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Toast.makeText(getActivity(), "FFMpeg Not Supported", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_converter, container, false);

        tvPath = (TextView) v.findViewById(R.id.tvPath);
        final EditText etFileName = (EditText) v.findViewById(R.id.etFileName);
        etFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    bConvert.setEnabled(true);
                    bConvert.animate().alpha(1).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    outPutName = editable.toString();
                } else {
                    bConvert.setEnabled(false);
                    bConvert.animate().alpha(0).setInterpolator(new AccelerateDecelerateInterpolator()).start();

                }
            }
        });


        cpb = (CircularProgressButton) v.findViewById(R.id.btnWithText);
        bConvert = (CircularProgressButton) v.findViewById(R.id.bConvert);
        bConvert.setProgress(0);
        bConvert.setEnabled(false);
        cpb.setProgress(0);
        cpb.setOnClickListener(new View.OnClickListener()

                               {
                                   @Override
                                   public void onClick(View view) {
                                       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                           if (ActivityCompat.checkSelfPermission(getActivity(),
                                                   Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                   != PackageManager.PERMISSION_GRANTED) {

                                               // Should we show an explanation?
                                               showStoragePermission();
                                           } else {
                                               cpb.setProgress(0);
                                               pickFile();
                                           }
                                       }
                                   }
                               }

        );

        bConvert.setOnClickListener(new View.OnClickListener()

                                    {
                                        @Override
                                        public void onClick(View view) {
                /*String cmd = "-y -i " + inputPath + " " + Environment.getExternalStorageDirectory() + "/" + outPutName + "." + outPutFormat;
                Toast.makeText(getActivity(), cmd, Toast.LENGTH_SHORT).show();*/

                                            startConversion();
                                        }
                                    }

        );

        spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner.setAdapter(ArrayAdapter.createFromResource(

                getActivity(), R

                        .array.output_formats, android.R.layout.simple_spinner_dropdown_item));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

                                          {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                  //Toast.makeText(getActivity(),"Format : "+ adapterView.getItemAtPosition(i),Toast.LENGTH_SHORT).show();
                                                  outPutFormat = (String) adapterView.getItemAtPosition(i);
                                                  //bConvert.animate().alpha(bConvert.getAlpha() > 0 ? 0 : 1).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) {

                                              }
                                          }

        );

        return v;
    }

    public void showStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    pickFile();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                } else {

                    Toast.makeText(getActivity(), "F U!!", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        // i.setType("pdf/*");


        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);


        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, 777);
    }

    public void startConversion() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_loading);


        // set the custom dialog components - text, image and button
        final AnimatedCircleLoadingView loader = (AnimatedCircleLoadingView) dialog.findViewById(R.id.circle_loading_view);
        final Button bDone = (Button) dialog.findViewById(R.id.bDone);

        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        loader.startDeterminate();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        String cmd = "-y -i " + inputPath + " " + Environment.getExternalStorageDirectory() + "/" + outPutName + "." + outPutFormat;
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Toast.makeText(getActivity(), "Started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(String message) {
                    //Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
                    Log.e("Progress", message);
                }

                @Override
                public void onFailure(String message) {
                    //Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
                    loader.stopFailure();
                    Log.e("Failure", message);
                }

                @Override
                public void onSuccess(String message) {
                    //Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
                    Log.e("Success", message);
                }

                @Override
                public void onFinish() {
                    loader.stopOk();
                    bDone.setVisibility(View.VISIBLE);
                    //Toast.makeText(getActivity(),"completed",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {

            //change button success
            cpb.setProgress(100);
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                            tvPath.setText(uri.getPath());
                            inputPath = uri.getPath();
                            try {
                                file = new File(new URI(uri.getPath()));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                                Log.e("File ", e.toString());
                            }
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            // Do something with the URI
                            tvPath.setText(uri.toString());
                            inputPath = uri.getPath();
                            try {
                                file = new File(new URI(uri.getPath()));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                // Do something with the URI
                tvPath.setText(uri.getPath());
                inputPath = uri.getPath();
                //etpath.setVisibility(View.VISIBLE);
                Log.e("File Path", uri.getPath());
                file = new File(uri.getPath().toString());

            }
        } else {
            Toast.makeText(getActivity(), "Error opening file!", Toast.LENGTH_SHORT).show();
            cpb.setProgress(-1);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
