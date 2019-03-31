package com.lc.monitor.settings;

public enum InputType {

    EMAIL(0),

    PHONE(1);

    private int value;

    InputType(int value){
        this.value = value;
    }


    public int getValue(){
        return value;
    }
}
