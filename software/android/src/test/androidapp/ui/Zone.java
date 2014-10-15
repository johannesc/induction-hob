package test.androidapp.ui;

import test.androidapp.MainActivity;
import test.androidapp.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

/**
 * UI for a zone control. In the future me might wan't to add more support for things like:
 * - Show if a program is running or not for this zone
 * - Show current target power level
 * - Show if the zone is hot or not
 * - Make it possible to start a program from the zone?
 * - Show "selected" zone
 * - Perhaps we should do some of above using a compound view instead?
 */
public class Zone extends Button {
    private static final String LOG_TAG = MainActivity.LOG_TAG + " Zone";

    public String delimiter;
    public boolean fancyText;

    int offsetX;
    int offsetY;
    boolean viewAdded = false;
    final View floatingDialog;
    private int level = 0;
    RelativeLayout rl;
    private ChangeListener changeListener;

    DisplayMetrics metrics = new DisplayMetrics();

    public Zone(Context context) {
        this(context, null);
        Log.w(LOG_TAG, "JC Default constructor");
    }

    public Zone(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.w(LOG_TAG, "JC Normal constructor");
        floatingDialog = createPopupView(context);
    }

    public Zone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.w(LOG_TAG, "Creating Zone defstyle=" + defStyle + " attrs=" + attrs);
        floatingDialog = createPopupView(context);
    }

    private View createPopupView(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View floatingDialog = inflater.inflate(R.layout.seekbar, null, false);
        final SeekBar floatSeekBar = (SeekBar) floatingDialog.findViewById(R.id.seekBar);
        floatSeekBar.setOnSeekBarChangeListener(new SeekBarListener());
        floatSeekBar.setProgress(level);
        return floatingDialog;
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                changeListener.onLevelChanged(Zone.this, progress);
            }
        }
    }

    private String getCharForLevel(int level) {
        final String[] levelToText = {"0", "U", "1", "2", "3", "4", "5", "6", "7", "8", "9", "P"};
        return levelToText[level];
    }

    private void updateText() {
        setText(getCharForLevel(level));
    }

     MotionEvent downEvent = null;

     @Override
     public boolean onTouchEvent(MotionEvent event) {
         boolean handled = false;

         ViewParent parent = getParent();
         if (rl == null) {
             if (parent instanceof RelativeLayout) {
                 Log.w(LOG_TAG, "Got my view group!");
                 rl = (RelativeLayout) parent;

                 LayoutParams params = rl.getLayoutParams();
                 Log.w(LOG_TAG, "layout  width = " + rl.getWidth());
                 // Make fixed size so it does not resize when we popup the slider
                 params.width = rl.getWidth();
                 rl.setLayoutParams(params);

             } else {
                 Log.w(LOG_TAG, "Parent must be a RelativeLayout:" + parent);
                 return false;
             }
         }

         if (event.getAction() == MotionEvent.ACTION_DOWN) {

             floatingDialog.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

             Log.w(LOG_TAG, "w2=" + floatingDialog.getMeasuredWidth() + " h2=" + floatingDialog.getMeasuredHeight());

             Log.w(LOG_TAG, "v.getX()=" + getX() + " v.getY()=" + getY());

             int w = floatingDialog.getMeasuredWidth();
             int h = floatingDialog.getMeasuredHeight();

             RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);

             offsetX = -((w / 12) * level + w/12) + (int)event.getX();
             offsetY = - (h / 4) * 3 + (int)event.getY();
             params.leftMargin = offsetX + (int)getX();
             params.topMargin += offsetY + (int)getY() - 100;

             rl.addView(floatingDialog, params);
             if (downEvent != null) {
                 downEvent.recycle();
             }
             downEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() - offsetX, event.getY()- offsetY, event.getMetaState());
             // Post the down event so the dialog is inflated first
             post(new Runnable() {
                @Override
                public void run() {
                    if (downEvent != null) {
                        floatingDialog.dispatchTouchEvent(downEvent);
                        downEvent.recycle();
                        downEvent = null;
                    }
                }
             });
             viewAdded = true;
             handled = true;
         } else if (event.getAction() == MotionEvent.ACTION_UP) {
             MotionEvent newEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() - offsetX, event.getY()- offsetY, event.getMetaState());
             floatingDialog.dispatchTouchEvent(newEvent);
             newEvent.recycle();
             newEvent = null;

             rl.removeView(floatingDialog);
             viewAdded = false;
             handled = true;
         } else if (viewAdded){
             MotionEvent newEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() - offsetX, event.getY()- offsetY, event.getMetaState());
             floatingDialog.dispatchTouchEvent(newEvent);
             newEvent.recycle();
             newEvent = null;
             handled = true;
         }
         return handled;
     }

     public interface ChangeListener {
         public void onLevelChanged(Zone zone, int level);
     }

     public void setChangeListener(ChangeListener changeListener) {
         this.changeListener = changeListener;
     }

     public void setCurrentLevel(int level) {
         this.level = level;
         updateText();
     }
}
