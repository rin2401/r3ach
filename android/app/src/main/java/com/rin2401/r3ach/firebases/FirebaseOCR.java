package com.rin2401.r3ach.firebases;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions.Builder;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionText.Line;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirebaseOCR {
    public static Comparator<Line> LineComparator = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int i;
            int i2;
            if (Math.abs(line.getBoundingBox().top - line2.getBoundingBox().top) <= 4) {
                i = line.getBoundingBox().left;
                i2 = line2.getBoundingBox().left;
            } else {
                i = line.getBoundingBox().top;
                i2 = line2.getBoundingBox().top;
            }
            return i - i2;
        }
    };
    public static Comparator<TextBlock> TextBlockComparator = new Comparator<TextBlock>() {
        public int compare(TextBlock textBlock, TextBlock textBlock2) {
            int i;
            int i2;
            if (Math.abs(textBlock.getBoundingBox().top - textBlock2.getBoundingBox().top) <= 4) {
                i = textBlock.getBoundingBox().left;
                i2 = textBlock2.getBoundingBox().left;
            } else {
                i = textBlock.getBoundingBox().top;
                i2 = textBlock2.getBoundingBox().top;
            }
            return i - i2;
        }
    };
    private String ansA;
    private String ansB;
    private String ansC;
    private FirebaseVisionTextRecognizer detector;
    private FirebaseVisionImage image;
    private OnOCRListenner ocrListener;
    FirebaseVisionCloudTextRecognizerOptions options;
    private String question;
    private Task<FirebaseVisionText> result;
    private SharedPreferences sharedPreferences;
    private boolean recogOnDevice;


    public interface OnOCRListenner {
        void OnError();

        void OnSuccess();
    }

    public FirebaseOCR(Context context) {
        String str = "";
        this.question = str;
        this.ansA = str;
        this.ansB = str;
        this.ansC = str;
        this.sharedPreferences = context.getSharedPreferences("setting", 0);
        this.recogOnDevice = this.sharedPreferences.getBoolean("recog_type", true);
    }

    public void resetAllStatus() {
        String str = "";
        this.question = str;
        this.ansA = str;
        this.ansB = str;
        this.ansC = str;
    }

    public void detectFromBitmap(Bitmap bitmap) {
        resetAllStatus();
        this.image = FirebaseVisionImage.fromBitmap(bitmap);
        if (this.recogOnDevice) {
            this.detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        } else {
            this.options = new Builder().setLanguageHints(Arrays.asList(new String[]{"vi"})).build();
            this.detector = FirebaseVision.getInstance().getCloudTextRecognizer(this.options);
        }
        this.result = this.detector.processImage(this.image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                int i;
                ArrayList arrayList = new ArrayList();
                for (FirebaseVisionText.TextBlock lines : ((FirebaseVisionText) FirebaseOCR.this.result.getResult()).getTextBlocks()) {
                    for (Line add : lines.getLines()) {
                        arrayList.add(add);
                    }
                }
                Line[] lineArr = (Line[]) arrayList.toArray(new Line[arrayList.size()]);
                Arrays.sort(lineArr, FirebaseOCR.LineComparator);
                ArrayList arrayList2 = new ArrayList();
                int i2 = 0;
                int i3 = -1;
                int i4 = -1;
                for (Line text : lineArr) {
                    String text2 = text.getText();
                    arrayList2.add(text2);
                    if (text2.contains("?")) {
                        i4++;
                        i3 = i4;
                    } else {
                        i4++;
                    }
                }
                int size = arrayList2.size();
                String str = "";
                String str2 = " ";
                if (i3 >= 0) {
                    while (i2 <= i3) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(str);
                        stringBuilder.append((String) arrayList2.get(i2));
                        stringBuilder.append(str2);
                        str = stringBuilder.toString();
                        i2++;
                    }
                    FirebaseOCR.this.question = str;
                    i = i3 + 1;
                    if (i < size) {
                        FirebaseOCR.this.ansA = (String) arrayList2.get(i);
                    }
                    i = i3 + 2;
                    if (i < size) {
                        FirebaseOCR.this.ansB = (String) arrayList2.get(i);
                    }
                    i3 += 3;
                    if (i3 < size) {
                        FirebaseOCR.this.ansC = (String) arrayList2.get(i3);
                    }
                } else if (size >= 4) {
                    while (true) {
                        i3 = size - 3;
                        if (i2 >= i3) {
                            break;
                        }
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(str);
                        stringBuilder2.append((String) arrayList2.get(i2));
                        stringBuilder2.append(str2);
                        str = stringBuilder2.toString();
                        i2++;
                    }
                    FirebaseOCR.this.question = str;
                    FirebaseOCR.this.ansA = (String) arrayList2.get(i3);
                    FirebaseOCR.this.ansB = (String) arrayList2.get(size - 2);
                    FirebaseOCR.this.ansC = (String) arrayList2.get(size - 1);
                }
                Log.d("OCR", FirebaseOCR.this.question);
                Log.d("OCR", FirebaseOCR.this.ansA);
                Log.d("OCR", FirebaseOCR.this.ansB);
                Log.d("OCR", FirebaseOCR.this.ansC);
                FirebaseOCR.this.ocrListener.OnSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(@NonNull Exception exception) {
                FirebaseOCR.this.ocrListener.OnError();
            }
        });
    }

    public void setOCRListener(OnOCRListenner onOCRListenner) {
        this.ocrListener = onOCRListenner;
    }

    public String getAsentQuestion() {
        return this.question.toLowerCase();
    }

    public String getAsentAnsA() {
        return this.ansA.toLowerCase();
    }

    public String getAsentAnsB() {
        return this.ansB.toLowerCase();
    }

    public String getAsentAnsC() {
        return this.ansC.toLowerCase();
    }

    public String get0AsentQuestion() {
        return removeAccent(this.question).toLowerCase();
    }

    public String get0AsentAnsA() {
        return removeAccent(this.ansA).toLowerCase();
    }

    public String get0AsentAnsB() {
        return removeAccent(this.ansB).toLowerCase();
    }

    public String get0AsentAnsC() {
        return removeAccent(this.ansC).toLowerCase();
    }

    public String getAsentExQuestion() {
        return this.question.toLowerCase();
    }

    public String get0AsentExQuestion() {
        String replaceAll = removeAccent(this.question).replaceAll("\\.{2,4}", "*");
        Matcher matcher = Pattern.compile("(\\w)(\\s+)([A-Z][\\w']*(?:\\s+[A-Z][\\w']*)*)([(\\s+)(\\W)])").matcher(replaceAll);
        while (matcher.find()) {
            String group = matcher.group(3);
            replaceAll = replaceAll.replaceFirst(group, "\"" + group + "\"");
        }
        return replaceAll.toLowerCase();
    }

    public static String removeAccent(String str) {
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
