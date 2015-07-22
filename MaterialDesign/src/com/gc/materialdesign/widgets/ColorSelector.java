package com.gc.materialdesign.widgets;

import com.gc.materialdesign.R;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.views.Slider.OnValueChangedListener;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class ColorSelector extends android.app.Dialog implements OnValueChangedListener{
	
	int color = Color.BLACK;
	Context context;
	View colorView;
	View view, backView;//background

	OnColorSelectedListener onColorSelectedListener;
	Slider red, green, blue;

	ButtonFlat ok, cancel;

	android.app.Dialog that;
	

	public ColorSelector(Context context,Integer color, OnColorSelectedListener onColorSelectedListener) {
		super(context, android.R.style.Theme_Translucent);
		this.context = context;
		this.onColorSelectedListener = onColorSelectedListener;
		that = this;
		if(color != null)
			this.color = color;
	}
	

	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.color_selector);
	    
	    view = (LinearLayout)findViewById(R.id.contentSelector);
		backView = (RelativeLayout)findViewById(R.id.rootSelector);
		backView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getX() < view.getLeft() || event.getX() >view.getRight()
						|| event.getY() > view.getBottom() || event.getY() < view.getTop()) {
					dismiss();
				}
				return false;
			}
		});

		ok = (ButtonFlat) findViewById(R.id.ok);
		cancel = (ButtonFlat) findViewById(R.id.cancel);

		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(ColorSelector.this.onColorSelectedListener != null) {
					ColorSelector.this.onColorSelectedListener.onColorSelected(ColorSelector.this.color);
					that.dismiss();
				}
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				that.cancel();
			}
		});

		colorView = findViewById(R.id.viewColor);
	    colorView.setBackgroundColor(color);

		// Resize ColorView
		colorView.post(new Runnable() {

			@Override
			public void run() {
				LinearLayout.LayoutParams params = (LayoutParams) colorView.getLayoutParams();
				boolean isPortrait = getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT;

				if (isPortrait) {
					params.height = colorView.getWidth();
				} else {
					params.width = colorView.getHeight();
				}
				colorView.setLayoutParams(params);
			}
		});


	    // Configure Sliders
	    red = (Slider) findViewById(R.id.red);
	    green = (Slider) findViewById(R.id.green);
	    blue = (Slider) findViewById(R.id.blue);
	    
	    int r = (this.color >> 16) & 0xFF;
		int g = (this.color >> 8) & 0xFF;
		int b = (this.color >> 0) & 0xFF;
		
		red.setValue(r);
		green.setValue(g);
		blue.setValue(b);
		
		red.setOnValueChangedListener(this);
		green.setOnValueChangedListener(this);
		blue.setOnValueChangedListener(this);
	}

	@Override
	public void show() {
		super.show();
		view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_main_show_amination));
		backView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_root_show_amin));
	}
	
	@Override
	public void onValueChanged(int value) {
		color = Color.rgb(red.getValue(), green.getValue(), blue.getValue());
		colorView.setBackgroundColor(color);
	}
	
	
	// Event that execute when color selector is closed
	public interface OnColorSelectedListener{
		public void onColorSelected(int color);
	}
		
	@Override
	public void dismiss() {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_main_hide_amination);
		
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.post(new Runnable() {
					@Override
					public void run() {
						ColorSelector.super.dismiss();
			        }
			    });
			}
		});
		
		Animation backAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_root_hide_amin);
		
		view.startAnimation(anim);
		backView.startAnimation(backAnim);
	}

	public int getScreenOrientation()
	{
		int orientation = 0;
		if (colorView.getWidth() > colorView.getHeight()) {
			orientation = Configuration.ORIENTATION_PORTRAIT;
		} else {
			orientation = Configuration.ORIENTATION_LANDSCAPE;
		}
		return orientation;
	}

}
