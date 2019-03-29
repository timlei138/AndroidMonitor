package com.lc.monitor.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lc.monitor.R;


public class CustomSeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private String TAG = getClass().getSimpleName();
    private TextView mLabel;
    private int mMax = 100;
    private int mProgress;
    private String mLabelTag;
    private boolean showLabel;
    private boolean mTrackingTouch;
    private Callback mCallback;

    public CustomSeekBarPreference(Context context) {
        this(context,null);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet,0);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i,0);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attributeSet, int i, int i1) {
        super(context, attributeSet, i, i1);
    }

    private void setProgress(int progress, boolean sync) {
        if (progress > mMax) {
            progress = mMax;
        }

        if (progress < 0) {
            progress = 0;
        }
        if (progress != mProgress) {
            mProgress = progress;
            persistInt(progress);
            if (sync) {
                notifyChanged();
            }
        }

    }

    public void setCallback(Callback callback){
        this.mCallback = callback;
    }

    public void setLabelTag(String tag){
        showLabel = true;
        mLabelTag = tag;
    }

    public int getProgress() {
        return this.mProgress;
    }

    public CharSequence getSummary() {
        return null;
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SeekBar seekBar = (SeekBar)holder.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(this.mMax);
        seekBar.setProgress(this.mProgress);
        seekBar.setEnabled(isEnabled());
        mLabel = (TextView) holder.findViewById(R.id.seekbar_label);
        if(showLabel){
            mLabel.setVisibility(View.VISIBLE);
            mLabel.setText(getTitle());
        }else{
            mLabel.setVisibility(View.GONE);
        }
    }



    protected Object onGetDefaultValue(TypedArray var1, int var2) {
        return var1.getInt(var2, 0);
    }



    public boolean onKey(View parent, int var2, KeyEvent keyevent) {
        if (keyevent.getAction() != KeyEvent.ACTION_UP) {
            if (var2 == 81 || var2 == 70) {
                this.setProgress(1 + this.getProgress());
                return true;
            }

            if (var2 == 69) {
                this.setProgress(-1 + this.getProgress());
                return true;
            }
        }

        return false;
    }

    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
        } else {
            SavedState savedState = (SavedState)parcelable;
            super.onRestoreInstanceState(savedState.getSuperState());
            mProgress = savedState.progress;
            mMax = savedState.max;
            notifyChanged();
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (this.isPersistent()) {
            return parcelable;
        } else {
            SavedState savedState = new SavedState(parcelable);
            savedState.progress = mProgress;
            savedState.max = mMax;
            return savedState;
        }
    }

    protected void onSetInitialValue(boolean sync, Object obj) {
        int progress;
        if (sync) {
            progress = this.getPersistedInt(this.mProgress);
        } else {
            progress = (Integer)obj;
        }

        this.setProgress(progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        Log.e(TAG,"onProgressChanged i:"+i+",fromUser:"+fromUser+",mTrackingTouch:"+mTrackingTouch+",mCallback:"+mCallback);
        if (fromUser && !mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
        if(mCallback!=null){
            mCallback.onStartingChangedCallback();
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != mProgress) {
            syncProgress(seekBar);
            if(mCallback != null){
                mCallback.ProgressChangedCallback(getKey(),mProgress);
            }
        }


    }

    public void setMax(int max) {
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }

    }

    public void setProgress(int progress) {
        this.setProgress(progress, true);
    }

    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress != mProgress) {
            if (!callChangeListener(progress)) {
                seekBar.setProgress(mProgress);
                return;
            }
            setProgress(progress, false);
        }

    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int var1) {
                return new SavedState[var1];
            }
        };
        int max;
        int progress;

        public SavedState(Parcel parcel) {
            super(parcel);
            this.progress = parcel.readInt();
            this.max = parcel.readInt();
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            var1.writeInt(this.progress);
            var1.writeInt(this.max);
        }
    }

    public interface Callback{
        void onStartingChangedCallback();
        void ProgressChangedCallback(String key, int progress);
    }
}
