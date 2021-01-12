package com.rin2401.r3ach.dialogs;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.rin2401.r3ach.Constans;
import com.rin2401.r3ach.R;

public class AppInfoDialog extends Dialog {
    public AppInfoDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_appinfo);
        findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AppInfoDialog.this.dismiss();
            }
        });
    }

    public void show() {
        super.show();
        getWindow().setLayout((int) (((double) Constans.WIDTH) / 1.2d), -2);
    }
}
