package com.gpa.safecharge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaceHolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaceHolderFragment extends Fragment {

    private static final String INSTRUCTION_PARAM = "instruction";
    private static final String ANIMATION_PARAM = "animation";
    private static final String COLOR_PARAM = "color";

    private String instruction;
    private Integer animation;
    private Integer color;

    public PlaceHolderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param instruction Parameter 1.
     * @return A new instance of fragment PlaceHolderFragment.
     */
    public static PlaceHolderFragment newInstance(String instruction,int animation,int color) {
        PlaceHolderFragment fragment = new PlaceHolderFragment();
        Bundle args = new Bundle();
        args.putString(INSTRUCTION_PARAM, instruction);
        args.putInt(ANIMATION_PARAM,animation);
        args.putInt(COLOR_PARAM,color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            instruction = getArguments().getString(INSTRUCTION_PARAM);
            animation = getArguments().getInt(ANIMATION_PARAM);
            color = getArguments().getInt(COLOR_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_place_holder, container, false);
        view.setBackgroundColor(color);
        TextView textView = view.findViewById(R.id.fragment_textview);
        textView.setText(instruction);
        LottieAnimationView animationView = view.findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(animation);
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setRepeatMode(LottieDrawable.REVERSE);
        animationView.playAnimation();
        return view;
    }
}