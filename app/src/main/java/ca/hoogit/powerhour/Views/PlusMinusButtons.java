package ca.hoogit.powerhour.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.hoogit.powerhour.R;

/**
 * Created by jordon on 10/07/15.
 */
public class PlusMinusButtons extends LinearLayout {

    private static final String TAG = PlusMinusButtons.class.getSimpleName();

    private ButtonInteraction mListener;
    private int width;
    private int height;

    @Bind({R.id.minus_10, R.id.minus_1, R.id.reset, R.id.plus_1, R.id.plus_10})
    List<ImageButton> mButtons;

    @OnClick(R.id.minus_10)
    public void minusTen() {
        if (mListener != null) {
            mListener.minusPressed(10);
        }
    }

    @OnClick(R.id.minus_1)
    public void minusOne() {
        if (mListener != null) {
            mListener.minusPressed(1);
        }
    }

    public PlusMinusButtons(Context context) {
        super(context);
        init(context, null);
    }

    public PlusMinusButtons(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlusMinusButtons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attributeSet) {
        inflate(getContext(), R.layout.plus_minus_buttons, this);
        ButterKnife.bind(this);

        // Get the attributes
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.PlusMinusButtons, 0, 0);
        width = attr.getDimensionPixelOffset(R.styleable.PlusMinusButtons_pmb_width, 0);
        height = attr.getDimensionPixelOffset(R.styleable.PlusMinusButtons_pmb_height, 0);
        attr.recycle();

        setupButtons();
    }

    private void setupButtons() {
        for (ImageButton button : mButtons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.height = height != 0 ? height : params.height;
            params.width = width != 0 ? width : params.width;
            button.setLayoutParams(params);
        }
    }

    private void registerListeners() {
        for (ImageButton b : mButtons) {
            switch (b.getId()) {
                case R.id.minus_10:
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.minusPressed(10);
                        }
                    });
                    break;
                case R.id.minus_1:
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.minusPressed(1);
                        }
                    });
                    break;
                case R.id.reset:
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.resetPressed();
                        }
                    });
                    break;
                case R.id.plus_1:
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.plusPressed(1);
                        }
                    });
                    break;
                case R.id.plus_10:
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mListener.plusPressed(10);
                        }
                    });
                    break;
                default:
                    Log.e(TAG, "Invalid button with id: " + b.getId());
            }
        }
    }

    public ButtonInteraction getListener() {
        return mListener;
    }

    public void setOnButtonPressed(ButtonInteraction mListener) {
        this.mListener = mListener;
        registerListeners();
    }

    public void removeOnButtonPressed() {
        this.mListener = null;
        for (ImageButton b : mButtons) {
            b.setOnClickListener(null);
        }
    }

    public interface ButtonInteraction {
        void minusPressed(int amount);

        void resetPressed();

        void plusPressed(int amount);
    }
}
