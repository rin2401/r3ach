package com.rin2401.r3ach;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.vision.text.TextRecognizer.Builder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextRecogConf {
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
    public static Comparator<Text> TextComparator = new Comparator<Text>() {
        public int compare(Text text, Text text2) {
            int i;
            int i2;
            if (Math.abs(text.getBoundingBox().top - text2.getBoundingBox().top) <= 4) {
                i = text.getBoundingBox().left;
                i2 = text2.getBoundingBox().left;
            } else {
                i = text.getBoundingBox().top;
                i2 = text2.getBoundingBox().top;
            }
            return i - i2;
        }
    };
    private String ansA;
    private String ansB;
    private String ansC;
    private String question;
    private TextRecognizer textRecognizer;

    public TextRecogConf(Context context) {
        String str = "";
        this.question = str;
        this.ansA = str;
        this.ansB = str;
        this.ansC = str;
        this.textRecognizer = new Builder(context.getApplicationContext()).build();
    }

    public void resetAllStatus() {
        String str = "";
        this.question = str;
        this.ansA = str;
        this.ansB = str;
        this.ansC = str;
    }

    public void recogFromBitmap(Bitmap bitmap) {
        String str;
        String str2;
        StringBuilder stringBuilder;
        resetAllStatus();
        SparseArray detect = this.textRecognizer.detect(new Frame.Builder().setBitmap(bitmap).build());
        ArrayList arrayList = new ArrayList();
        int i = 0;
        int i2 = 0;
        while (true) {
            str = "--";
            str2 = "RECOG";
            if (i2 >= detect.size()) {
                break;
            }
            TextBlock textBlock = (TextBlock) detect.valueAt(i2);
            for (Text text : textBlock.getComponents()) {
                arrayList.add(text);
                stringBuilder = new StringBuilder();
                stringBuilder.append(textBlock.getBoundingBox().top);
                stringBuilder.append(" :: ");
                stringBuilder.append(text.getBoundingBox().left);
                stringBuilder.append(str);
                stringBuilder.append(text.getValue());
                Log.d(str2, stringBuilder.toString());
            }
            Log.d(str2, "--------------------------------");
            i2++;
        }
        Log.d(str2, "================================");
        Text[] textArr = (Text[]) arrayList.toArray(new Text[arrayList.size()]);
        Arrays.sort(textArr, TextComparator);
        arrayList = new ArrayList();
        int i3 = -1;
        int i4 = -1;
        for (Text text2 : textArr) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(text2.getBoundingBox().left);
            stringBuilder.append(str);
            stringBuilder.append(text2.getValue());
            Log.d(str2, stringBuilder.toString());
            String value = text2.getValue();
            arrayList.add(value);
            if (value.contains("?")) {
                i4++;
                i3 = i4;
            } else {
                i4++;
            }
        }
        int size = arrayList.size();
        String str3 = "";
        str = " ";
        if (i3 >= 0) {
            while (i <= i3) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(str3);
                stringBuilder2.append((String) arrayList.get(i));
                stringBuilder2.append(str);
                str3 = stringBuilder2.toString();
                i++;
            }
            this.question = str3;
            i = i3 + 1;
            if (i < size) {
                this.ansA = (String) arrayList.get(i);
            }
            i = i3 + 2;
            if (i < size) {
                this.ansB = (String) arrayList.get(i);
            }
            i3 += 3;
            if (i3 < size) {
                this.ansC = (String) arrayList.get(i3);
            }
        } else if (size >= 4) {
            while (true) {
                i3 = size - 3;
                if (i < i3) {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(str3);
                    stringBuilder3.append((String) arrayList.get(i));
                    stringBuilder3.append(str);
                    str3 = stringBuilder3.toString();
                    i++;
                } else {
                    this.question = str3;
                    this.ansA = (String) arrayList.get(i3);
                    this.ansB = (String) arrayList.get(size - 2);
                    this.ansC = (String) arrayList.get(size - 1);
                    return;
                }
            }
        }
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

    public String get0AsentExQuestion() {
        String replaceAll = removeAccent(this.question).replaceAll("\\.{2,4}", "*");
        Matcher matcher = Pattern.compile("(\\w)(\\s+)([A-Z][\\w']*(?:\\s+[A-Z][\\w']*)*)([(\\s+)(\\W)])").matcher(replaceAll);
        while (matcher.find()) {
            String group = matcher.group(3);
            StringBuilder stringBuilder = new StringBuilder();
            String str = "\"";
            stringBuilder.append(str);
            stringBuilder.append(group);
            stringBuilder.append(str);
            replaceAll = replaceAll.replaceFirst(group, stringBuilder.toString());
        }
        return replaceAll.toLowerCase();
    }

    public static String removeAccent(String str) {
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
