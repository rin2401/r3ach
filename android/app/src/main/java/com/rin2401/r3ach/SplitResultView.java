package com.rin2401.r3ach;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SplitResultView extends LinearLayout {
    private LinearLayout ln1;
    private LinearLayout ln2;
    private LinearLayout ln3;
    private TextView tv11;
    private TextView tv12;
    private TextView tv21;
    private TextView tv22;
    private TextView tv31;
    private TextView tv32;

    public SplitResultView(Context context) {
        super(context);
    }

    public SplitResultView(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SplitResultView(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public SplitResultView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.ln1 = (LinearLayout) findViewById(R.id.ln1);
        this.ln2 = (LinearLayout) findViewById(R.id.ln2);
        this.ln3 = (LinearLayout) findViewById(R.id.ln3);
        this.tv11 = (TextView) findViewById(R.id.tv11);
        this.tv12 = (TextView) findViewById(R.id.tv12);
        this.tv21 = (TextView) findViewById(R.id.tv21);
        this.tv22 = (TextView) findViewById(R.id.tv22);
        this.tv31 = (TextView) findViewById(R.id.tv31);
        this.tv32 = (TextView) findViewById(R.id.tv32);
    }

    public void setDataResult(String str) {
        try {
            String[] split = str.split("<br>");
            this.tv11.setText(split[0]);
            this.tv12.setText(split[1]);
            this.tv21.setText(split[2]);
            this.tv22.setText(split[3]);
            this.tv31.setText(split[4]);
            this.tv32.setText(split[5]);
            int parseInt = Integer.parseInt(split[1]);
            int parseInt2 = Integer.parseInt(split[3]);
            int parseInt3 = Integer.parseInt(split[5]);
            String str2 = "#500000FF";
            if (parseInt > parseInt2 && parseInt > parseInt3) {
                this.ln1.setBackgroundColor(Color.parseColor(str2));
            } else if (parseInt2 > parseInt && parseInt2 > parseInt3) {
                this.ln2.setBackgroundColor(Color.parseColor(str2));
            } else if (parseInt3 > parseInt && parseInt3 > parseInt2) {
                this.ln3.setBackgroundColor(Color.parseColor(str2));
            }
        } catch (Exception unused) {
        }
    }
}
