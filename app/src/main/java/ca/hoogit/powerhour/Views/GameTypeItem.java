package ca.hoogit.powerhour.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import at.markushi.ui.CircleButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Configure.GameOptions;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.ColorUtil;

/**
 * Created by jordon on 07/07/15.
 */
public class GameTypeItem extends LinearLayout {

    @Bind(R.id.option_container) LinearLayout mLayout;
    @Bind(R.id.option_click) LinearLayout mRippleView;
    @Bind(R.id.option_icon) ImageView mIcon;
    @Bind(R.id.option_title) TextView mTitle;
    @Bind(R.id.option_configure) CircleButton mConfigureButton;

    private GameOptions mOptions;

    public GameTypeItem(Context context) {
        super(context);
        init(context, null);
    }

    public GameTypeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GameTypeItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GameTypeItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attributeSet) {
        inflate(getContext(), R.layout.game_type_item, this);
        ButterKnife.bind(this);

        // Get the attributes
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.GameTypeItem, 0, 0);

        int backgroundColor = attr.getColor(R.styleable.GameTypeItem_gti_background, getResources().getColor(R.color.primary));
        int accentColor = attr.getColor(R.styleable.GameTypeItem_gti_accent, getResources().getColor(R.color.accent));

        mLayout.setBackgroundColor(backgroundColor);
        mIcon.setImageResource(attr.getResourceId(R.styleable.GameTypeItem_gti_icon, R.drawable.ic_action));
        mTitle.setText(attr.getString(R.styleable.GameTypeItem_gti_title));

        boolean hideButton = attr.getBoolean(R.styleable.GameTypeItem_gti_hide_button, false);
        int buttonColor = ColorUtil.darken(backgroundColor);
        int type = attr.getInteger(R.styleable.GameTypeItem_gti_type, 0);
        attr.recycle();

        // Setup the items
        mOptions = GameOptions.getDefault(GameOptions.intToType(type));
        mOptions.setBackgroundColor(backgroundColor);
        mOptions.setAccentColor(accentColor);
        if (hideButton) {
            mConfigureButton.setVisibility(View.GONE);
        } else {
            mConfigureButton.setFocusable(false);
            mConfigureButton.setColor(buttonColor);
        }
    }

    public GameOptions getOptions() {
        return mOptions;
    }

    public boolean hasButton() {
        return mConfigureButton.getVisibility() == View.VISIBLE;
    }

    public void setItemOnClick(View.OnClickListener l) {
        mRippleView.setOnClickListener(l);
    }

    public void setConfigureOnClick(View.OnClickListener l) {
        mConfigureButton.setOnClickListener(l);
    }
}
