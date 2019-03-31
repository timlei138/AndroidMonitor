package com.lc.monitor.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.lc.monitor.R;

import java.util.regex.Pattern;

public class CustomEditPreference extends Preference {

    private String TAG = getClass().getSimpleName();

    private String mEditHintStr;
    private Drawable mIcons;
    private boolean mSwitchDefaultValue;
    private SharedPreferences mSharedPreference;
    private String key;
    private String savedValue;
    private boolean savedToggle;

    private EditText mEditText;


    private boolean dataVaild = false;

    private int inputType;

    public CustomEditPreference(Context context) {
        this(context,null);
    }

    public CustomEditPreference(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomEditPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }


    public CustomEditPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingEditor);
        mSwitchDefaultValue = typedArray.getBoolean(R.styleable.SettingEditor_switch_stats,false);
        mEditHintStr = typedArray.getString(R.styleable.SettingEditor_editor_hint);
        mIcons = typedArray.getDrawable(R.styleable.SettingEditor_editor_icon);
        inputType = typedArray.getInt(R.styleable.SettingEditor_input_type,InputType.EMAIL.getValue());
        typedArray.recycle();
        setPersistent(false);
        mSharedPreference = getSharedPreferences();
        key = getKey();



    }



    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSharedPreference = getSharedPreferences();
        ImageView imageView = (ImageView) holder.findViewById(R.id.icon);
        mEditText = (EditText) holder.findViewById(R.id.edit);
        Switch switchButton = (Switch) holder.findViewById(R.id.switch_toggle);
        imageView.setBackground(mIcons);

        mEditText.setHint(mEditHintStr);
        switchButton.setChecked(mSwitchDefaultValue);
        switchButton.setOnCheckedChangeListener(checkChangedListener);
        mEditText.setOnFocusChangeListener(editorFocusChangedListener);
        savedValue = mSharedPreference.getString(key+"_value","");
        if(!TextUtils.isEmpty(savedValue)){
            mEditText.setText(savedValue);
        }
        savedToggle = mSharedPreference.getBoolean(key+"_toggle",false);
        switchButton.setChecked(savedToggle);

        if(inputType == InputType.PHONE.getValue()){
            mEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PHONETIC);
        }
    }




    private View.OnFocusChangeListener editorFocusChangedListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            String input = mEditText.getText().toString();
            Log.d(TAG,"input "+input);
            if(!hasFocus && !TextUtils.isEmpty(mEditText.getText())){
                boolean dataVaild = false;
                if(inputType == InputType.EMAIL.getValue()){
                    if(checkEmail(input)){
                        dataVaild = true;
                    }else{
                        Toast.makeText(getContext(),"请输入正确的Email地址",Toast.LENGTH_SHORT).show();
                    }
                }else if(inputType == InputType.PHONE.getValue()){
                    if(checkPhone(input)){
                        dataVaild = true;
                    }else{
                        Toast.makeText(getContext(),"请输入正确的电话号码",Toast.LENGTH_SHORT).show();
                    }
                }
                if(dataVaild){
                    mSharedPreference.edit().putString(key+"_value",mEditText.getText().toString()).commit();
                    savedValue = mEditText.getText().toString();
                }

            }else{

            }
        }
    };

    private CompoundButton.OnCheckedChangeListener checkChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(savedToggle != isChecked && dataVaild){
                mSharedPreference.edit().putBoolean(key+"_toggle",isChecked).commit();
                savedToggle = isChecked;
            }
        }
    };

    private boolean checkPhone(String phone){
        String regex = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";
        return Pattern.matches(regex,phone);
    }

    private boolean checkEmail(String email){
        String regex = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return Pattern.matches(regex,email);
    }
}
