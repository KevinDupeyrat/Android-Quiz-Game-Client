package iut.paci.classroomcomunnity.frame;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import iut.paci.classroomcomunnity.R;


public class PresentFragment extends Fragment {


    public PresentFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Modification du titre
        getActivity().setTitle("Qui est là ?");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_present, container, false);
    }
}