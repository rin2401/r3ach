package com.rin2401.r3ach;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebSearch {
    private Context context;
    private ArrayList<AsyncTask> listAsyncTask = new ArrayList();
    private WebSearchListener searchListener;

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private static final String TAG = "DownloadTask";

        private DownloadTask() {
        }

        public String doInBackground(String... google) {
            ArrayList urlList = new ArrayList();
            urlList.clear();
            try {
                Document document = Jsoup.connect(google[0]).get();
                if (document != null) {
                    Matcher matcher = Pattern.compile("<a href=\"(.*?)\" ping").matcher(document.html());
                    while (matcher.find()) {
                        try {
                            String url = matcher.group(1);
                            if (!(!url.startsWith("http") || url.contains("google") || url.contains("youtube") || url.contains("ted.com") || WebSearch.this.containLinkResult(urlList, url))) {
                                urlList.add(url);
                                Log.e("LINK", url);
                            }
                        } catch (Exception unused) {
                        }
                    }
                }
                WebSearch.this.searchListener.OnSuccess(urlList);
            } catch (IOException e) {
                e.printStackTrace();
//                WebSearch.this.searchListener.OnError();
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(String str) {
            super.onPostExecute(str);
        }
    }

    interface WebSearchListener {
        void OnError();

        void OnSuccess(ArrayList<String> arrayList);
    }

    public WebSearch(Context context) {
        this.context = context;
    }

    public void cancelAsyncTask() {
        Iterator it = this.listAsyncTask.iterator();
        while (it.hasNext()) {
            ((AsyncTask) it.next()).cancel(true);
        }
        this.listAsyncTask.clear();
    }

    public void searchWebResult(String str) {
        String url = "https://www.google.com/search?q=" + str;
        this.listAsyncTask.add(new DownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{url}));
    }

    private boolean containLinkResult(ArrayList<String> arrayList, String str) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (((String) it.next()).equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public void setWebSearchListener(WebSearchListener webSearchListener) {
        this.searchListener = webSearchListener;
    }
}
