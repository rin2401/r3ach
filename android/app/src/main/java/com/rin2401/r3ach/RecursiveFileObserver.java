package com.rin2401.r3ach;

import android.os.FileObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver {
    public static int CHANGES_ONLY = 2120;
    int mMask;
    List<SingleFileObserver> mObservers;
    String mPath;

    private class SingleFileObserver extends FileObserver {
        private String mPath;

        public SingleFileObserver(String str, int i) {
            super(str, i);
            this.mPath = str;
        }

        public void onEvent(int i, String str) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.mPath);
            stringBuilder.append("/");
            stringBuilder.append(str);
            RecursiveFileObserver.this.onEvent(i, stringBuilder.toString());
        }
    }

    public void onEvent(int i, String str) {
    }

    public RecursiveFileObserver(String str) {
        this(str, 4095);
    }

    public RecursiveFileObserver(String str, int i) {
        super(str, i);
        this.mPath = str;
        this.mMask = i;
    }

    public void startWatching() {
        if (this.mObservers == null) {
            int i;
            this.mObservers = new ArrayList();
            Stack stack = new Stack();
            stack.push(this.mPath);
            while (true) {
                i = 0;
                if (stack.empty()) {
                    break;
                }
                String str = (String) stack.pop();
                this.mObservers.add(new SingleFileObserver(str, this.mMask));
                File[] listFiles = new File(str).listFiles();
                if (listFiles != null) {
                    while (i < listFiles.length) {
                        if (!(!listFiles[i].isDirectory() || listFiles[i].getName().equals(".") || listFiles[i].getName().equals(".."))) {
                            stack.push(listFiles[i].getPath());
                        }
                        i++;
                    }
                }
            }
            while (i < this.mObservers.size()) {
                ((SingleFileObserver) this.mObservers.get(i)).startWatching();
                i++;
            }
        }
    }

    public void stopWatching() {
        if (this.mObservers != null) {
            for (int i = 0; i < this.mObservers.size(); i++) {
                ((SingleFileObserver) this.mObservers.get(i)).stopWatching();
            }
            this.mObservers.clear();
            this.mObservers = null;
        }
    }
}
