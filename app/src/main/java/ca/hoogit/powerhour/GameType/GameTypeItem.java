package ca.hoogit.powerhour.GameType;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.library.RippleView;

import at.markushi.ui.CircleButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.R;

/**
 * Created by jordon on 07/07/15.
 */
public class GameTypeItem extends LinearLayout {

    @Bind(R.id.option_container) LinearLayout mLayout;
    @Bind(R.id.option_icon) ImageView mIcon;
    @Bind(R.id.option_title) TextView mTitle;
    @Bind(R.id.option_configure) CircleButton mConfigureButton;

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

        mLayout.setBackgroundColor(attr.getColor(R.styleable.GameTypeItem_gti_background, R.color.primary));
        mIcon.setImageResource(attr.getResourceId(R.styleable.GameTypeItem_gti_icon, R.drawable.ic_action));
        mTitle.setText(attr.getString(R.styleable.GameTypeItem_gti_title));

        boolean hideButton = attr.getBoolean(R.styleable.GameTypeItem_gti_hide_button, false);
        int buttonColor = attr.getColor(R.styleable.GameTypeItem_gti_button_color, R.color.accent);
        int type = attr.getInteger(R.styleable.GameTypeItem_gti_type, 0);
        attr.recycle();

        // Setup the items
        if (hideButton) {
            mConfigureButton.setVisibility(View.GONE);
        } else {
            GameOptions options = new GameOptions(mTitle.getText().toString(), type);
            mConfigureButton.setFocusable(false);
            mConfigureButton.setColor(buttonColor);
            mConfigureButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Configure button pressed, value: " + mTitle, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
