package com.rin2401.r3ach;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class ReadContent {
    private ReadContentListener contentListener;
    private Context context;
    private ArrayList<AsyncTask> listAsyncTask;
    private int numSearch = 2;
    private int searchCount = 0;

    public interface ReadContentListener {
        void OnOneLinkSuccess(String str, String str2);

        void OnSuccess(String str, String str2);
    }

    private class ReadContentURL extends AsyncTask<String, Void, String> {
        private ReadContentURL() {
        }

        /* Access modifiers changed, original: protected */
        public void onPreExecute() {
            super.onPreExecute();
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            ReadContent readContent = ReadContent.this;
            readContent.searchCount = readContent.searchCount + 1;
            if (ReadContent.this.searchCount == ReadContent.this.numSearch) {
                String str2 = "";
                ReadContent.this.contentListener.OnSuccess(str2, str2);
            }
        }

        /* Access modifiers changed, original: protected|varargs */
        public String doInBackground(String... strArr) {
            String str = "";
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(strArr[0]).openStream()));
                String str2 = str;
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(str2);
                    stringBuilder.append(readLine);
                    str2 = stringBuilder.toString();
                }
                String stripHtml = ReadContent.this.stripHtml(str2.toLowerCase());
                ReadContent.this.contentListener.OnOneLinkSuccess(stripHtml, ReadContent.removeAccent(stripHtml));
            } catch (MalformedURLException unused) {
                ReadContent.this.contentListener.OnOneLinkSuccess(str, str);
            } catch (IOException e) {
                e.printStackTrace();
                ReadContent.this.contentListener.OnOneLinkSuccess(str, str);
            }
            return null;
        }
    }

    public ReadContent(Context context) {
        this.context = context;
        this.listAsyncTask = new ArrayList();
    }

    public void cancelAsyncTask() {
        Iterator it = this.listAsyncTask.iterator();
        while (it.hasNext()) {
            ((AsyncTask) it.next()).cancel(true);
        }
        this.listAsyncTask.clear();
    }

    public void getAllContent(ArrayList<String> arrayList, int i) {
        this.searchCount = 0;
        this.numSearch = i;
        i = arrayList.size();
        if (i < this.numSearch) {
            this.numSearch = i;
        }
        if (arrayList.size() <= 0) {
            String str = "";
            this.contentListener.OnSuccess(str, str);
            return;
        }
        for (i = 0; i < this.numSearch; i++) {
            ReadContentURL readContentURL = new ReadContentURL();
            readContentURL.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{(String) arrayList.get(i)});
            this.listAsyncTask.add(readContentURL);
        }
    }

    public void setReadContentListener(ReadContentListener readContentListener) {
        this.contentListener = readContentListener;
    }

    public String stripHtml(String str) {
        return String.valueOf(Html.fromHtml(str));
    }

    public static String removeAccent(String str) {
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
