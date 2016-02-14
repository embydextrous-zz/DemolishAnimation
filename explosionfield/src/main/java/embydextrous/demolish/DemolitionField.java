package embydextrous.demolish;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class DemolitionField extends View {

    private List<DemolitionAnimator> mDemolitions = new ArrayList<>();
    private int[] mExpandInset = new int[2];
    private DemolitionListener demolitionListener;

    public DemolitionField(Context context) {
        super(context);
        init();
    }

    public DemolitionField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DemolitionField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Arrays.fill(mExpandInset, Utils.dp2Px(32));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DemolitionAnimator demolition : mDemolitions) {
            demolition.draw(canvas);
        }
    }

    public void expandDemolitionBound(int dx, int dy) {
        mExpandInset[0] = dx;
        mExpandInset[1] = dy;
    }

    public void demolish(Bitmap bitmap, Rect bound, long startDelay, long duration) {
        final DemolitionAnimator demolition = new DemolitionAnimator(this, bitmap, bound);
        demolition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDemolitions.remove(animation);
                if (mDemolitions.isEmpty())
                    demolitionListener.onDemolished();

            }
        });
        demolition.setStartDelay(startDelay);
        demolition.setDuration(duration);
        mDemolitions.add(demolition);
        demolitionListener.onDemolitionStarted();
        demolition.start();
    }

    public void demolish(final View view) {
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);

            }
        });
        animator.start();
        view.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        demolish(Utils.createBitmapFromView(view), r, startDelay, DemolitionAnimator.DEFAULT_DURATION);
    }

    public void clear() {
        mDemolitions.clear();
        invalidate();
    }

    public static DemolitionField attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        DemolitionField demolitionField = new DemolitionField(activity);
        rootView.addView(demolitionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return demolitionField;
    }

    public void setOnDemolitionListener(DemolitionListener demolitionListener) {
        this.demolitionListener = demolitionListener;
    }

    private interface DemolitionListener{
        void onDemolitionStarted();
        void onDemolished();
    }
}
