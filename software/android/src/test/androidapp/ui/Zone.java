package test.androidapp.ui;

import java.util.Set;

import test.androidapp.InductionController.TemperatureReading;
import test.androidapp.MainActivity;
import test.androidapp.R;
import test.androidapp.ZoneController;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

/**
 * UI for a zone control. In the future me might wan't to add more support for things like:
 * - Show if a program is running or not for this zone
 * - Show current temperature for this zone
 * - Show current target power level
 * - Make it possible to start a program from the zone?
 * - Show "selected" zone
 * - Perhaps we should do some of above using a compound view instead?
 */
public class Zone extends TextView {
    private static final String LOG_TAG = MainActivity.LOG_TAG + " Zone";

    public String delimiter;
    public boolean fancyText;
    public ZoneController programRunning;

    int offsetX;
    int offsetY;
    boolean viewAdded = false;
    private int level = 0;
    private boolean hot;
    private boolean present = true;
    private int icon = R.drawable.zone_front_left;

    DisplayMetrics metrics = new DisplayMetrics();

    @SuppressWarnings("unused")
    private int targetLevel;

    private boolean powered;

    public Zone(Context context) {
        this(context, null);
        Log.w(LOG_TAG, "JC Default constructor");
    }

    public Zone(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.w(LOG_TAG, "JC Normal constructor");
        getAttribute(context, attrs);
    }

    public Zone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.w(LOG_TAG, "Creating Zone defstyle=" + defStyle + " attrs=" + attrs);
        getAttribute(context, attrs);
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Zone, 0, 0);
        Log.w(LOG_TAG, "icon =" + icon);
        icon = a.getResourceId(R.styleable.Zone_icon, icon);
        Log.w(LOG_TAG, "icon =" + icon);
        a.recycle();
    }

    private String getCharForLevel(int level) {
        final String[] levelToText = {"0", "U", "1", "2", "3", "4", "5", "6", "7", "8", "9", "P"};
        return levelToText[level];
    }

    private void updateText() {
        setTextColor(hot ? Color.RED : Color.WHITE);
        if (powered) {
            StringBuilder text = new StringBuilder();
            if (present || level == 0) {
                text.append(getCharForLevel(level));
            } else {
                text.append("F");
            }
            //text += "\n" + getCharForLevel(targetLevel);
            if (programRunning != null) { //TODO extract info about controller, e.g. temp, target temp etc
                ZoneController.Type type = programRunning.getType();
                if (type == ZoneController.Type.MILK) {
                    text.append(" milk");
                } else if (type == ZoneController.Type.TARGET_TEMP) {
                    Set<Byte> sensors = programRunning.getUsedSensorAddresses();
                    SparseArray<TemperatureReading> temps = programRunning.getCurrentTemperatures();
                    text.append(" ").append((int)programRunning.getTargetTemperatures()[0]);
                    for (Byte adr : sensors) {
                        text.append("(").append(temps.get(adr).temperature).append(")");
                    }
                } else {
                    text.append(" ?prog");
                }
            }
            //Log.w(LOG_TAG, "Setting text to \"" + text + "\"");
            setText(text);
        } else {
            if (hot) {
                setText("H");
            } else {
                setText(R.string.defaultZoneText);
            }
        }

        if (present || level == 0 || !powered) {
            if (getAnimation() != null) {
                clearAnimation();
            }
        } else {
            if (getAnimation() == null) {
                // A custom animation that blinks the text by changing the alpha value
                Animation anim = new AlphaAnimation(0.0f, 1.0f) {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        t.setAlpha(interpolatedTime > 0.5 ? 1 : 0);
                    }
                };
                anim.setDuration(1000);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                startAnimation(anim);
            }
        }
    }

    public void setCurrentLevel(int level) {
        this.level = level;
        updateText();
    }

    public void setHot(boolean hot) {
        this.hot = hot;
        updateText();
    }

    public void setPotPresent(boolean present) {
        this.present = present;
        updateText();
    }

    public void setTargetPowerLevel(int target) {
        this.targetLevel = target;
        updateText();
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        updateText();
    }

    public void setProgramRunning(ZoneController programRunning) {
        this.programRunning = programRunning;
        updateText();
    }
}
