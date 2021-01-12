package com.rin2401.r3ach;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 24;
    private static final int REQUEST_SCREENSHOT = 39;

    private MediaProjectionManager mediaProjectionManager;
    private String host;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                host = ((EditText) findViewById(R.id.host)).getText().toString();
                username = ((EditText) findViewById(R.id.username)).getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                }

                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Draw over other app permission not available.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Take screenshot permission not available.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                startService(new Intent(MainActivity.this, Moon.class)
                        .putExtra("resultCode", resultCode)
                        .putExtra("data", data)
                        .putExtra("host", host)
                        .putExtra("username", username)
                );
                finish();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
