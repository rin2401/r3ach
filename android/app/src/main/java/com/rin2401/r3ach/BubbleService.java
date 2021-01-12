package com.rin2401.r3ach;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.display.VirtualDisplay;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjection.Callback;
import android.media.projection.MediaProjectionManager;
import android.os.Build.VERSION;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import org.jetbrains.annotations.Nullable;
import androidx.core.internal.view.SupportMenu;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.rin2401.r3ach.ReadContent.ReadContentListener;
import com.rin2401.r3ach.firebases.FirebaseOCR;
import com.rin2401.r3ach.WebSearch.WebSearchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BubbleService extends Service{
    public static String EXTRA_RESULT_CODE = "RESULT_CODE";
    public static String EXTRA_RESULT_INTENT = "RESULT_INTENT";
    private static String STORE_DIRECTORY = null;
    static final int VIRT_DISPLAY_FLAGS = 9;
    private ArrayAdapter<String> adapter;
    private final ToneGenerator beeper;
    private View collapsedView;
    private CountDownTimer countDownTimer;
    private View expandedView;
    private FirebaseOCR firebaseOCR;
    private Handler handler;
    private final HandlerThread handlerThread;
    private ImageView imvConfetti;
    private ImageTransmogrifier it;
    private LinearLayout lnAnylist;
    private LinearLayout lnCapture;
    private ListView lvSplitResult;
    private View mFloatingView;
    private WindowManager mWindowManager;
    private MediaProjectionManager mgr;
    private boolean nagativeQuestion;
    private int oneLinkKeyCount;
    private int oneLinkNormalCount;
    private LayoutParams params;
    private ProgressBar progressBar;
    private MediaProjection projection;

    private String ansA1;
    private String ansA2;
    private String ansB1;
    private String ansB2;
    private String ansC1;
    private String ansC2;
    private int freqA = 0;
    private int freqAKey = 0;
    private int freqANormal = 0;
    private int freqB = 0;
    private int freqBKey = 0;
    private int freqBNormal = 0;
    private int freqC = 0;
    private int freqCKey = 0;
    private int freqCNormal = 0;
    private String question1;
    private String question2;
    private String questionWithKey;
    private int quoteACount;
    private String quoteAResult;
    private int quoteBCount;
    private String quoteBResult;
    private int quoteCCount;
    private String quoteCResult;
    private ReadContent readContentKey;
    private ReadContent readContentNormal;
    private String result0AsentKey;
    private String result0AsentNormal;
    private String resultAsentKey;
    private String resultAsentNormal;
    private int resultCode;
    private Intent resultData;
    private ArrayList<String> splitResults;
    private ArrayList<String> totalLinkResults;
    private TextView tvAnsA;
    private TextView tvAnsAKey;
    private TextView tvAnsANormal;
    private TextView tvAnsB;
    private TextView tvAnsBKey;
    private TextView tvAnsBNormal;
    private TextView tvAnsC;
    private TextView tvAnsCKey;
    private TextView tvAnsCNormal;
    private TextView tvQuestion;
    private TextView tvStatistic;
    private TextView tvTimer;
    private VirtualDisplay vdisplay;
    private WebSearch webSearchKey;
    private WebSearch webSearchNormal;
    private WindowManager wmgr;

    public BubbleService() {
        this.handlerThread = new HandlerThread(getClass().getSimpleName(), 10);
        this.beeper = new ToneGenerator(3, 100);
        this.countDownTimer = new CountDownTimer(8500, 100) {
            BubbleService bs = BubbleService.this;

            public void onTick(long tick) {
                bs.tvTimer.setText(String.format("%02d : %02d", tick/1000, tick/100));
                if (tick <= 3000) {
                    bs.progressBar.setVisibility(View.INVISIBLE);
                    bs.tvTimer.setTextColor(SupportMenu.CATEGORY_MASK);
                }
            }

            public void onFinish() {
                bs.tvTimer.setText("00 : 00");
                bs.webSearchKey.cancelAsyncTask();
                bs.webSearchNormal.cancelAsyncTask();
                bs.readContentKey.cancelAsyncTask();
                bs.readContentNormal.cancelAsyncTask();
            }
        };
    }

    public void onCreate() {
        super.onCreate();
        this.mgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.wmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
        this.totalLinkResults = new ArrayList();

        this.firebaseOCR = new FirebaseOCR(getApplicationContext());
        this.readContentKey = new ReadContent(getApplicationContext());
        this.readContentKey.setReadContentListener(new ReadContentListener() {
            public void OnSuccess(String str, String str2) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                    }
                });
            }

            public void OnOneLinkSuccess(final String str, final String str2) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (str.length() > 0 || str2.length() > 0) {
                            BubbleService bs = BubbleService.this;
                            bs.resultAsentKey += str;
                            bs.result0AsentKey += str2;
                            bs.caculateResult(bs.ansA1, bs.ansB1, bs.ansC1, str, true);
                            bs.caculateResult(bs.ansA2, bs.ansB2, bs.ansC2, str2, true);
                            bs.oneLinkKeyCount = bs.oneLinkKeyCount + 1;
                            if (bs.oneLinkKeyCount >= 2 && bs.oneLinkNormalCount >= 6) {
                                bs.progressBar.setVisibility(View.INVISIBLE);
                            }

                            Log.d("COUNT", bs.oneLinkKeyCount + "--" + bs.oneLinkNormalCount);
                        }
                    }
                });
            }
        });
        this.readContentNormal = new ReadContent(getApplicationContext());
        this.readContentNormal.setReadContentListener(new ReadContentListener() {
            public void OnSuccess(String str, String str2) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                    }
                });
            }

            public void OnOneLinkSuccess(final String str, final String str2) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (str.length() > 0 || str2.length() > 0) {
                            BubbleService bs = BubbleService.this;
                            bs.resultAsentKey += str;
                            bs.result0AsentKey += str2;
                            bs.caculateResult(bs.ansA1, bs.ansB1, bs.ansC1, str, true);
                            bs.caculateResult(bs.ansA2, bs.ansB2, bs.ansC2, str2, true);
                            bs.oneLinkKeyCount = bs.oneLinkKeyCount + 1;
                            if (bs.oneLinkKeyCount >= 2 && bs.oneLinkNormalCount >= 6) {
                                bs.progressBar.setVisibility(View.INVISIBLE);
                            }

                            Log.d("COUNT", bs.oneLinkKeyCount + "--" + bs.oneLinkNormalCount);
                        }
                    }
                });
            }
        });
        this.webSearchKey = new WebSearch(getApplicationContext());
        this.webSearchKey.setWebSearchListener(new WebSearchListener() {
            public void OnSuccess(final ArrayList<String> arrayList) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (arrayList.size() > 0) {
                            BubbleService.this.readContentKey.getAllContent(arrayList, 2);
                        }
                    }
                });
            }

            public void OnError() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        Toast.makeText(BubbleService.this.getApplicationContext(), "Có lổi xảy ra!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        this.webSearchNormal = new WebSearch(getApplicationContext());
        this.webSearchNormal.setWebSearchListener(new WebSearchListener() {
            public void OnSuccess(final ArrayList<String> arrayList) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (BubbleService.this.totalLinkResults.size() <= 0) {
                            BubbleService.this.totalLinkResults.addAll(arrayList);
                        } else {
                            for (int size = arrayList.size() - 1; size >= 0; size--) {
                                Object obj;
                                String str = (String) arrayList.get(size);
                                Iterator it = BubbleService.this.totalLinkResults.iterator();
                                while (it.hasNext()) {
                                    String str2 = (String) it.next();
                                    if (str2.equalsIgnoreCase(str)) {
                                        BubbleService.this.totalLinkResults.remove(str2);
                                        obj = 1;
                                        break;
                                    }
                                }
                                obj = null;
                                if (obj == null) {
                                    BubbleService.this.totalLinkResults.add(0, str);
                                }
                            }
                        }
                        BubbleService.this.readContentNormal.getAllContent(BubbleService.this.totalLinkResults, 3);
                    }
                });
            }

            public void OnError() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        Toast.makeText(BubbleService.this.getApplicationContext(), "Có lổi xảy ra!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        this.mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_bubblet, null);
        this.lnCapture = (LinearLayout) this.mFloatingView.findViewById(R.id.lnCapture);
        this.lnAnylist = (LinearLayout) this.mFloatingView.findViewById(R.id.lnAnylist);


        this.params = new LayoutParams(-1, -2, VERSION.SDK_INT >= 26 ? 2038 : 2002, 8, -3);
        this.params.gravity = 51;
        this.params.x = 0;
        this.params.y = 0;
        this.params.width = Constans.WIDTH;
        this.params.height = getSharedPreferences("setting", 0).getInt("capture_height", Constans.HEIGHT / 2);
        this.mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.mWindowManager.addView(this.mFloatingView, this.params);
        this.collapsedView = this.mFloatingView.findViewById(R.id.collapseView);
        this.expandedView = this.mFloatingView.findViewById(R.id.expandView);
        this.mFloatingView.findViewById(R.id.imvConvertExpand).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.showExpandView();
            }
        });
        this.mFloatingView.findViewById(R.id.imvConvertCollapese).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.showCollapseView();
            }
        });
        this.mFloatingView.findViewById(R.id.imvCloseCollapse).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.stopSelf();
            }
        });
        this.mFloatingView.findViewById(R.id.imvCloseExpand).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.stopSelf();
            }
        });
        this.mFloatingView.findViewById(R.id.imvCapture).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.beeper.startTone(32);
                BubbleService.this.resetAllStatus();
                BubbleService.this.firebaseOCR.resetAllStatus();
                BubbleService.this.startCapture();
            }
        });
        this.mFloatingView.findViewById(R.id.imvSearch).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.progressBar.setVisibility(View.INVISIBLE);
                BubbleService.this.splitResults.clear();
                BubbleService.this.adapter.notifyDataSetChanged();
                for (int i = 1; i <= 3; i++) {
                    BubbleService.this.searchForSplitWord(i);
                }
            }
        });
        this.mFloatingView.findViewById(R.id.imvRefresh).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService bubbleService = BubbleService.this;
                bubbleService.stopService(new Intent(bubbleService, BubbleService.class));
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName(BubbleService.this, MainActivity.class));
                BubbleService.this.startActivity(intent);
            }
        });
        this.mFloatingView.findViewById(R.id.imvMinus).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.params.height -= 10;
                BubbleService.this.mWindowManager.updateViewLayout(BubbleService.this.mFloatingView, BubbleService.this.params);
                Editor edit = BubbleService.this.getApplicationContext().getSharedPreferences("setting", 0).edit();
                edit.putInt("capture_height", BubbleService.this.params.height);
                edit.commit();
            }
        });
        this.mFloatingView.findViewById(R.id.imvPlus).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                BubbleService.this.params.height += 10;
                BubbleService.this.mWindowManager.updateViewLayout(BubbleService.this.mFloatingView, BubbleService.this.params);
                Editor edit = BubbleService.this.getApplicationContext().getSharedPreferences("setting", 0).edit();
                edit.putInt("capture_height", BubbleService.this.params.height);
                edit.commit();
            }
        });
        this.mFloatingView.findViewById(R.id.tvTimer).setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                ImageView imageView = (ImageView) BubbleService.this.mFloatingView.findViewById(R.id.imvConfetti);
                if (imageView.getVisibility() == View.VISIBLE) {
                    imageView.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        this.progressBar = (ProgressBar) this.mFloatingView.findViewById(R.id.progressBar);
        this.tvQuestion = (TextView) this.mFloatingView.findViewById(R.id.tvQuestion);
        this.tvAnsA = (TextView) this.mFloatingView.findViewById(R.id.tvAnsA);
        this.tvAnsB = (TextView) this.mFloatingView.findViewById(R.id.tvAnsB);
        this.tvAnsC = (TextView) this.mFloatingView.findViewById(R.id.tvAnsC);
        this.tvStatistic = (TextView) this.mFloatingView.findViewById(R.id.tvStatistic);
        this.tvTimer = (TextView) this.mFloatingView.findViewById(R.id.tvTimer);
        this.tvAnsAKey = (TextView) this.mFloatingView.findViewById(R.id.tvAnsAKey);
        this.tvAnsBKey = (TextView) this.mFloatingView.findViewById(R.id.tvAnsBKey);
        this.tvAnsCKey = (TextView) this.mFloatingView.findViewById(R.id.tvAnsCKey);
        this.tvAnsANormal = (TextView) this.mFloatingView.findViewById(R.id.tvAnsANormal);
        this.tvAnsBNormal = (TextView) this.mFloatingView.findViewById(R.id.tvAnsBNormal);
        this.tvAnsCNormal = (TextView) this.mFloatingView.findViewById(R.id.tvAnsCNormal);
        this.lvSplitResult = (ListView) this.mFloatingView.findViewById(R.id.lvSplitResult);
        this.splitResults = new ArrayList();
        this.adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_spit_result, this.splitResults) {
            public View getView(int i, @Nullable View view, ViewGroup viewGroup) {
                if (view != null) {
                    return (SplitResultView) view.getTag();
                }
                view = new View(BubbleService.this.getApplicationContext());
                SplitResultView splitResultView = (SplitResultView) LayoutInflater.from(BubbleService.this.getApplicationContext()).inflate(R.layout.item_spit_result, null);
                splitResultView.setDataResult((String) BubbleService.this.splitResults.get(i));
                view.setTag(splitResultView);
                return splitResultView;
            }
        };
        this.lvSplitResult.setAdapter(this.adapter);
        this.imvConfetti = (ImageView) this.mFloatingView.findViewById(R.id.imvConfetti);
        this.mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new OnTouchListener() {
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action != 0) {
                    if (action != 1) {
                        if (action != 2) {
                            return false;
                        }
                        BubbleService.this.params.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                        BubbleService.this.params.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                        BubbleService.this.mWindowManager.updateViewLayout(BubbleService.this.mFloatingView, BubbleService.this.params);
                        BubbleService.this.lnCapture.requestLayout();
                    }
                    return true;
                }
                this.initialX = BubbleService.this.params.x;
                this.initialY = BubbleService.this.params.y;
                this.initialTouchX = motionEvent.getRawX();
                this.initialTouchY = motionEvent.getRawY();
                BubbleService.this.lnCapture.requestLayout();
                return true;
            }
        });

    }


    private int getNavigationBarHeight(Context context, int i) {
        Resources resources = context.getResources();
        i = resources.getIdentifier(i == 1 ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        return i > 0 ? resources.getDimensionPixelSize(i) : 0;
    }

    public boolean isNavigationBarAvailable() {
        return (KeyCharacterMap.deviceHasKey(4) && KeyCharacterMap.deviceHasKey(3)) ? false : true;
    }


    public void OnOCRSuccess() {
        this.showExpandView();

        this.questionWithKey = this.firebaseOCR.get0AsentExQuestion();
        this.question1 = this.firebaseOCR.getAsentQuestion();
        this.ansA1 = this.firebaseOCR.getAsentAnsA();
        this.ansB1 = this.firebaseOCR.getAsentAnsB();
        this.ansC1 = this.firebaseOCR.getAsentAnsC();
        this.question2 = this.firebaseOCR.get0AsentQuestion();
        this.ansA2 = this.firebaseOCR.get0AsentAnsA();
        this.ansB2 = this.firebaseOCR.get0AsentAnsB();
        this.ansC2 = this.firebaseOCR.get0AsentAnsC();

        String questionColor = this.questionWithKey;
        String temp;
        for (String negative: Constans.LIST_NAGATIVE){
            if (this.questionWithKey.contains(negative)) {
                questionColor = questionColor.replaceAll(negative, "<font color='#0000FF'><u>" + negative + "</u></font>");
                while (true) {
                    int index = this.question2.indexOf(negative);
                    if (index < 0) {
                        break;
                    }
                    temp = this.question1.substring(index, negative.length() + index);
                    this.question1 = this.question1.replaceAll(temp + " ", "");
                    temp = this.question2.substring(index, negative.length() + index);
                    this.question2 = this.question2.replaceAll(temp + " ", "");
                    temp = this.questionWithKey.substring(index, negative.length() + index);
                    this.questionWithKey = this.questionWithKey.replaceAll(temp + " ", "");
                    this.nagativeQuestion = true;
                }
            }
        }

        this.tvQuestion.setText(questionColor);
        this.tvAnsA.setText(this.ansA1);
        this.tvAnsB.setText(this.ansB1);
        this.tvAnsC.setText(this.ansC1);
        if (this.ansA1.length() <= 0 || this.ansA2.length() <= 0) {
            this.ansA1 = this.ansA2 = "";
        }
        if (this.ansB1.length() <= 0 || this.ansB2.length() <= 0) {
            this.ansB1 = this.ansB2 = "";
        }
        if (this.ansC1.length() <= 0 || this.ansC2.length() <= 0) {
            this.ansC1 = this.ansC2 = "";
        }

        Log.e("question1-2", this.question1 + " " + this.question2);


        if (this.question1.length() > 0 && this.question2.length() > 0) {
            this.countDownTimer.start();
            this.progressBar.setVisibility(View.VISIBLE);
            if (this.questionWithKey.contains("\"")) {
                this.oneLinkKeyCount = 0;
                this.webSearchKey.searchWebResult(this.questionWithKey);
            } else {
                this.oneLinkKeyCount = 2;
            }
            this.webSearchNormal.searchWebResult(this.question1);
            this.webSearchNormal.searchWebResult(this.question2);
        }
    }


    private void processImageRecog(Bitmap bitmap) {
        int navigationBarHeight = isNavigationBarAvailable() ? getNavigationBarHeight(getApplicationContext(), 1) : 0;
        int[] iArr = new int[2];
        this.lnCapture.getLocationOnScreen(iArr);
        bitmap = Bitmap.createScaledBitmap(Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmap, Constans.WIDTH, Constans.HEIGHT + navigationBarHeight, false), iArr[0], iArr[1], iArr[0] + Constans.WIDTH, this.lnCapture.getHeight()), Constans.WIDTH, this.lnCapture.getHeight(), false);
        this.imvConfetti.setImageBitmap(bitmap);
        this.firebaseOCR.detectFromBitmap(bitmap);
        this.firebaseOCR.setOCRListener(new FirebaseOCR.OnOCRListenner() {
            @Override
            public void OnError() {

            }

            @Override
            public void OnSuccess() {
                BubbleService.this.OnOCRSuccess();
            }
        });
    }




    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent.getAction() == null) {
            this.resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 1337);
            this.resultData = (Intent) intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        }
        return 2;
    }

    private void stopCapture() {
        MediaProjection mediaProjection = this.projection;
        if (mediaProjection != null) {
            mediaProjection.stop();
            this.vdisplay.release();
            this.projection = null;
        }
    }

    private void startCapture() {
        this.projection = this.mgr.getMediaProjection(this.resultCode, this.resultData);
        this.it = new ImageTransmogrifier(this);
        Callback callback = new Callback() {
            public void onStop() {
                BubbleService.this.vdisplay.release();
            }
        };
        this.vdisplay = this.projection.createVirtualDisplay("andshooter", this.it.getWidth(), this.it.getHeight(), getResources().getDisplayMetrics().densityDpi, 9, this.it.getSurface(), null, this.handler);
        this.projection.registerCallback(callback, this.handler);
    }

    private int calculateTextContainTextMost(String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str2);
        stringBuilder.append(" thjdhasnbbggsbajjssg");
        str2 = stringBuilder.toString();
        String str3 = "\\W";
        String[] split = str.split(str3);
        String[] split2 = str2.split(str3);
        int length = split.length;
        int length2 = split2.length;
        int i = 0;
        for (String str4 : split) {
            int i2 = 0 + length;
            if (i2 >= length2) {
                break;
            }
            for (int i3 = 0; i3 < i2; i3++) {
                if (split2[i3].equalsIgnoreCase(str4)) {
                    i++;
                    break;
                }
            }
        }
        int i4 = -1;
        if (i > -1) {
            i4 = i;
        }
        int i5 = 1;
        for (length++; length <= length2; length++) {
            CharSequence charSequence = split2[i5 - 1];
            CharSequence charSequence2 = split2[length - 1];
            if (str.contains(charSequence)) {
                i--;
            }
            if (str.contains(charSequence2)) {
                i++;
            }
            if (i > i4) {
                i4 = i;
            }
            i5++;
        }
        Log.d("VALUE", i4 + "");
        return i4;
    }

    public void onDestroy() {
        stopCapture();
        View view = this.mFloatingView;
        if (view != null) {
            this.mWindowManager.removeView(view);
        }
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("Binding not supported. Go away.");
    }

    /* Access modifiers changed, original: 0000 */
    public WindowManager getWindowManager() {
        return this.wmgr;
    }

    /* Access modifiers changed, original: 0000 */
    public Handler getHandler() {
        return this.handler;
    }

    /* Access modifiers changed, original: 0000 */
    public void processImage(final Bitmap bitmap) {
        stopCapture();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                BubbleService.this.processImageRecog(bitmap);
            }
        });
    }

    private void searchForSplitWord(final int i) {
        this.tvStatistic.setVisibility(View.GONE);
        this.lvSplitResult.setVisibility(View.VISIBLE);
        new Handler().post(new Runnable() {
            public void run() {
                BubbleService bs = BubbleService.this;
                String str;
                String str2 = "\\W+";
                List asList = Arrays.asList(bs.ansA2.split(str2));
                List asList2 = Arrays.asList(bs.ansB2.split(str2));
                List asList3 = Arrays.asList(bs.ansC2.split(str2));
                ArrayList arrayList = new ArrayList(asList);
                ArrayList arrayList2 = new ArrayList(asList2);
                ArrayList arrayList3 = new ArrayList(asList3);

                int size = arrayList.size();
                int size2 = arrayList2.size();
                int size3 = arrayList3.size();
                int size4 = arrayList.size();
                if (size2 > size4) {
                    size4 = size2;
                }
                if (size3 > size4) {
                    size4 = size3;
                }
                while (true) {
                    str = "ajshjasdas";
                    if (size >= size4) {
                        break;
                    }
                    arrayList.add(str);
                    size++;
                }
                while (size2 < size4) {
                    arrayList2.add(str);
                    size2++;
                }
                while (size3 < size4) {
                    arrayList3.add(str);
                    size3++;
                }
                size = size4 - i;
                for (size2 = 0; size2 <= size4 - size; size2++) {
                    String str3;
                    StringBuilder stringBuilder;
                    str = "";
                    String str4 = str;
                    String str5 = str4;
                    size3 = size2;
                    while (true) {
                        if (size3 >= size2 + size) {
                            break;
                        }
                        str += " " + (String) arrayList.get(size3) ;
                        str4 += " " + (String) arrayList2.get(size3) ;
                        str5 += " " + (String) arrayList3.get(size3) ;

                        size3++;
                    }
                    if (str.length() > 0 && str4.length() > 0 && str5.length() > 0) {
                        bs.caculateSplit(str, str4, str5, bs.result0AsentKey + " " + bs.result0AsentNormal);
                    }
                }
            }
        });
    }

    private void resetAllStatus() {
        this.tvStatistic.setVisibility(View.VISIBLE);
        this.lvSplitResult.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.mFloatingView.findViewById(R.id.imvConfetti).setVisibility(View.VISIBLE);
        this.totalLinkResults.clear();
        this.quoteACount = this.quoteBCount = this.quoteCCount = -1;
        this.quoteAResult = this.quoteBResult = this.quoteCResult = "";
        this.freqANormal = this.freqBNormal = this.freqCNormal = 0;
        this.freqAKey = this.freqBKey = this.freqCKey = 0;
        this.freqA = this.freqB = this.freqC = 0;
        this.ansC2 = this.ansC1 = this.ansB2 = this.ansB1 = this.ansA2 = this.ansA1 = "";
        this.question2 = this.question1 = "";
        this.result0AsentNormal = this.resultAsentNormal = this.result0AsentKey = this.resultAsentKey = "";
        this.oneLinkNormalCount = this.oneLinkKeyCount = 0;
        this.nagativeQuestion = false;
        this.tvQuestion.setText("");
        this.tvAnsA.setText("");
        this.tvAnsB.setText("");
        this.tvAnsC.setText("");
        this.tvStatistic.setText("");
        this.tvAnsAKey.setText("");
        this.tvAnsBKey.setText("");
        this.tvAnsCKey.setText("");
        this.tvAnsANormal.setText("");
        this.tvAnsBNormal.setText("");
        this.tvAnsCNormal.setText("");
        this.tvTimer.setText("00 : 00");
        this.tvTimer.setTextColor(-16776961);
        this.splitResults.clear();
    }

    private synchronized void caculateResult(String ansA, String ansB, String ansC, String html, boolean z) {
        int start;
        int end;
        String para;
        ansA = ansA.trim();
        ansB = ansB.trim();
        ansC = ansC.trim();
        html = html.trim();
        int length = html.length();
        String[] words = this.question2.split("\\W");
        Matcher matcher = Pattern.compile(ansA).matcher(html);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (start >= 100) {
                start -= 100;
            }
            if (end + 100 < length) {
                end += 100;
            }
            para = html.substring(start, end);
            int count = 0;
            for (CharSequence word : words) {
                if (para.contains(word)) {
                    count++;
                }
            }
            if (count > this.quoteACount) {
                this.quoteACount = count;
                this.quoteAResult = para;
                for (CharSequence word : words) {
                    this.quoteAResult = this.quoteAResult.replaceAll("[;\\-.,\\s]+" + word + "[;\\-.,\\s]+",  "<font color='#0000FF'>" + word + "</font>");
                }
                this.quoteAResult = this.quoteAResult.replaceAll(ansA, "<font color='#0000FF'>" + ansA + "</font>");
            }
            this.freqA++;
            if (z) {
                this.freqAKey++;
            } else {
                this.freqANormal++;
            }
        }
        matcher = Pattern.compile(ansB).matcher(html);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            int i = start - 100;
            if (i >= 0) {
                start = i;
            }
            i = end + 100;
            if (i < length) {
                end = i;
            }
            para = html.substring(start, end);
            i = 0;
            for (CharSequence contains : words) {
                if (para.contains(contains)) {
                    i++;
                }
            }
            if (i > this.quoteBCount) {
                this.quoteBCount = i;
                this.quoteBResult = para;
                for (int i2 = 0; i2 < words.length; i2++) {
                    para = "[;\\-.,\\s]+" + words[i2] + "[;\\-.,\\s]+";
                    this.quoteBResult = this.quoteBResult.replaceAll(para,  "<font color='#0000FF'>" + words[i2] + "</font>");
                }
                this.quoteBResult = quoteBResult.replaceAll(ansA, "<font color='#0000FF'>" + ansA + "</font>");
            }
            this.freqB++;
            if (z) {
                this.freqBKey++;
            } else {
                this.freqBNormal++;
            }
        }
        matcher = Pattern.compile(ansC).matcher(html);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            int i = start - 100;
            if (i >= 0) {
                start = i;
            }
            i = end + 100;
            if (i < length) {
                end = i;
            }
            para = html.substring(start, end);
            i = 0;
            for (CharSequence contains : words) {
                if (para.contains(contains)) {
                    i++;
                }
            }
            if (i > this.quoteCCount) {
                this.quoteCCount = i;
                this.quoteCResult = para;
                for (int i2 = 0; i2 < words.length; i2++) {
                    para = "[;\\-.,\\s]+" + words[i2] + "[;\\-.,\\s]+";
                    this.quoteCResult = quoteCResult.replaceAll(para,  "<font color='#0000FF'>" + words[i2] + "</font>");
                }
                this.quoteCResult = quoteCResult.replaceAll(ansA, "<font color='#0000FF'>" + ansA + "</font>");
            }
            this.freqC++;
            if (z) {
                this.freqCKey++;
            } else {
                this.freqCNormal++;
            }
        }
        this.tvAnsA.setText(this.ansA1 + " -- " + this.freqA);
        this.tvAnsB.setText(this.ansB1 + " -- " + this.freqB);
        this.tvAnsC.setText(this.ansC1 + " -- " + this.freqC);

        this.tvAnsAKey.setText(Integer.toString(this.freqAKey));
        this.tvAnsBKey.setText(Integer.toString(this.freqBKey));
        this.tvAnsCKey.setText(Integer.toString(this.freqCKey));

        this.tvAnsANormal.setText(Integer.toString(this.freqANormal));
        this.tvAnsBNormal.setText(Integer.toString(this.freqBNormal));
        this.tvAnsCNormal.setText(Integer.toString(this.freqCNormal));

        int choose = 0;
        if (this.nagativeQuestion) {
            choose = Math.min(Math.min(this.freqA, this.freqB), this.freqC);
        }
        else {
            choose = Math.max(Math.max(this.freqA, this.freqB), this.freqC);
        }

        this.tvAnsA.setBackgroundColor(Color.parseColor("#00000000"));
        this.tvAnsB.setBackgroundColor(Color.parseColor("#00000000"));
        this.tvAnsC.setBackgroundColor(Color.parseColor("#00000000"));

        if (this.freqA == choose)
            this.tvAnsA.setBackgroundColor(Color.parseColor("#500000FF"));
        if (this.freqB == choose)
            this.tvAnsB.setBackgroundColor(Color.parseColor("#500000FF"));
        if (this.freqC == choose)
            this.tvAnsC.setBackgroundColor(Color.parseColor("#500000FF"));

        this.tvStatistic.setText("");
        String text = "A. " + this.quoteAResult + "<br>";
        text += "---------------------------------------------<br>";
        text += "B. " + this.quoteBResult + "<br>";
        text += "---------------------------------------------<br>";
        text += "C. " + this.quoteCResult + "<br>";
        text += "---------------------------------------------<br>";
        this.tvStatistic.setMovementMethod(new ScrollingMovementMethod());
        this.tvStatistic.setText(Html.fromHtml(text));
    }

    private synchronized void caculateSplit(String str, String str2, String str3, String str4) {
        str = str.trim();
        str2 = str2.trim();
        str3 = str3.trim();
        str4 = " " + str4.trim() + " ";
        int i = 0;
        int i2 = 0;
        int i3 = 0;

        while (Pattern.compile(str).matcher(str4).find()) {
            i2++;
        }
        while (Pattern.compile(str2).matcher(str4).find()) {
            i3++;
        }
        while (Pattern.compile(str3).matcher(str4).find()) {
            i++;
        }
        ArrayList arrayList = this.splitResults;
        arrayList.add(str+"<br>"+i2+"<br>"+str2+"<br>"+i3+"<br>"+str3+"<br>"+i);
        this.adapter.notifyDataSetChanged();
    }

    private void showExpandView() {
        this.params.x = 0;
        this.params.y = 0;
        this.params.width = Constans.WIDTH;
        this.params.height = Constans.HEIGHT;
        this.expandedView.setVisibility(View.VISIBLE);
        this.collapsedView.setVisibility(View.GONE);
        this.mWindowManager.updateViewLayout(this.mFloatingView, this.params);
    }

    private void showCollapseView() {
        this.params.x = 0;
        this.params.y = 0;
        this.params.width = Constans.WIDTH;
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("setting", 0);
        this.params.height = prefs.getInt("capture_height", Constans.HEIGHT/2);

        this.expandedView.setVisibility(View.GONE);
        this.collapsedView.setVisibility(View.VISIBLE);
        this.mWindowManager.updateViewLayout(this.mFloatingView, this.params);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap createBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Drawable background = view.getBackground();
        if (background != null) {
            background.draw(canvas);
        } else {
            canvas.drawColor(-1);
        }
        view.draw(canvas);
        return createBitmap;
    }
}
