package com.gpa.safecharge;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class InstructionPageAdapter extends FragmentStatePagerAdapter
{
    public InstructionPageAdapter(@NonNull FragmentManager fm) {
        super(fm,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        int arrayPosition = position % Resources.values().length;
        String instruction = Resources.getInstructionFromId(arrayPosition);
        int animation = Resources.getAnimationFromId(arrayPosition);
        int color = Resources.getColorFromId(arrayPosition);
        return PlaceHolderFragment.newInstance(instruction,animation,color);
    }

    @Override
    public int getCount() {
        return Resources.values().length * 20;
    }
}
