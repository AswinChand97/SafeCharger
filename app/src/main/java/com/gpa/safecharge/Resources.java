package com.gpa.safecharge;

import android.graphics.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Resources
{
    ZERO(0,"Connect your phone to the charger",R.raw.charging, Color.parseColor("#000000")),
    ONE(1,"The application will alert you when the battery level has reached a safe limit.",R.raw.bell_gold,Color.parseColor("#330009")),
    TWO(2,"Disconnect your phone from the charger",R.raw.no_connection,Color.parseColor("#000033"));
    private String instruction;
    private int identifier;
    private int animation;
    private int color;
    private static final Map<Integer,String> idVsInstruction = Collections.unmodifiableMap(initializeInstructionLookup());
    private static final Map<Integer,Integer> idVsAnimation= Collections.unmodifiableMap(initializeAnimationLookup());
    private static final Map<Integer,Integer> idVsColor= Collections.unmodifiableMap(initializeColorLookup());
    Resources(int identifier,String instruction,int animation,int color)
    {
        this.identifier = identifier;
        this.instruction = instruction;
        this.animation = animation;
        this.color = color;
    }

    public String getInstruction()
    {
        return this.instruction;
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public int getAnimation(){return this.animation;}

    public int getColor(){return this.color;}

    private static Map<Integer,String> initializeInstructionLookup()
    {
        Map<Integer,String> instructionLookup = new HashMap<>();
        for(Resources r : Resources.values())
        {
            instructionLookup.put(r.getIdentifier(),r.getInstruction());
        }
        return instructionLookup;
    }
    private static Map<Integer,Integer> initializeAnimationLookup()
    {
        Map<Integer,Integer> animationLookup = new HashMap<>();
        for(Resources r : Resources.values())
        {
            animationLookup.put(r.getIdentifier(),r.getAnimation());
        }
        return animationLookup;
    }

    private static Map<Integer,Integer> initializeColorLookup()
    {
        Map<Integer,Integer> colorLookup = new HashMap<>();
        for(Resources r : Resources.values())
        {
            colorLookup.put(r.getIdentifier(),r.getColor());
        }
        return colorLookup;
    }

    public static String getInstructionFromId(int identifier)
    {
        return idVsInstruction.get(identifier);
    }

    public static Integer getAnimationFromId(int identifier) {
        return idVsAnimation.get(identifier);
    }

    public static Integer getColorFromId(int identifier) {
        return idVsColor.get(identifier);
    }
}
