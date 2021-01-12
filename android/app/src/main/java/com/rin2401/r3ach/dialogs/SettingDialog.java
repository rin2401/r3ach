package com.rin2401.r3ach.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import com.rin2401.r3ach.Constans;
import com.rin2401.r3ach.R;

public class SettingDialog extends Dialog {
    private Button btnOK;
    private boolean offline;
    private RadioButton rbDevice;
    private RadioButton rbCloud;
    private RadioGroup rgRecognizer;
    private SharedPreferences sharedPreferences;

    public SettingDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_setting);
        this.sharedPreferences = context.getSharedPreferences("setting", 0);
        this.offline = this.sharedPreferences.getBoolean("offline", true);
        this.rgRecognizer = findViewById(R.id.rgRecognizer);
        this.rbDevice = findViewById(R.id.rbRecogOnDevice);
        this.rbCloud = findViewById(R.id.rbRecogOnCloud);
        this.rbDevice.setChecked(this.offline);
        this.rbCloud.setChecked(!this.offline);

        this.rgRecognizer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SettingDialog sd = SettingDialog.this;
                sd.offline = checkedId==R.id.rbRecogOnDevice;
                Editor edit = sd.sharedPreferences.edit();
                edit.putBoolean("offline", sd.offline);
                edit.commit();
            }
        });

        this.btnOK = findViewById(R.id.btnOK);
        this.btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SettingDialog.this.dismiss();
            }
        });
    }

    public void show() {
        super.show();
        getWindow().setLayout((int) (((double) Constans.WIDTH) / 1.2d), -2);
    }
}
