package com.ttdevs.wheel.widget;

import java.text.DecimalFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttdevs.wheel.R;
import com.ttdevs.wheel.widget.TuneWheel.OnValueChangeListener;

/**
 * 此view的封装意义不大，仅仅用于展示效果<br>
 * 若要使用，定义一些接口对外暴露即可
 * 
 * @author ttdevs
 * @version create：2014年8月26日
 */
public class TuneWheelFull extends LinearLayout implements OnClickListener, OnValueChangeListener {

	private DecimalFormat mFormat = new DecimalFormat("###.#");
	private int mPadding = 10, mPortionSize = 150;
	private float mDensity;

	private TuneWheel twWheel;
	private TextView tvValue, tvUnit, tvAlert;
	private Button btGram, btPortion;

	private boolean isGram = true;

	public TuneWheelFull(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView();
	}

	private void initView() {
		mDensity = getContext().getResources().getDisplayMetrics().density;
		mPadding *= mDensity;

		setOrientation(LinearLayout.VERTICAL);
		setPadding(mPadding, mPadding, mPadding, mPadding);
		LayoutInflater.from(getContext()).inflate(R.layout.view_tunewheel_full, this);

		tvValue = (TextView) findViewById(R.id.tvValue);
		tvUnit = (TextView) findViewById(R.id.tvUnit);
		tvAlert = (TextView) findViewById(R.id.tvAlert);
		tvAlert.setVisibility(View.GONE);

		twWheel = (TuneWheel) findViewById(R.id.twWheel);
		twWheel.setValueChangeListener(this);
		twWheel.initViewParam(50, 500, TuneWheel.MOD_TYPE_ONE);
		
		btGram = (Button) findViewById(R.id.btGram);
		btPortion = (Button) findViewById(R.id.btPortion);
		btGram.setOnClickListener(this);
		btPortion.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btGram:
			tvUnit.setText("克");
			twWheel.initViewParam(50, 500, TuneWheel.MOD_TYPE_ONE);
			tvAlert.setVisibility(View.GONE);
			isGram = true;
			break;
		case R.id.btPortion:
			tvUnit.setText("份");
			twWheel.initViewParam(20, 100, TuneWheel.MOD_TYPE_HALF);
			tvAlert.setText(String.valueOf(20 * mPortionSize) + "克");
			tvAlert.setVisibility(View.VISIBLE);
			isGram = false;
			break;

		default:
			break;
		}
	}

	@Override
	public void onValueChange(float value) {
		if (!isGram) {
			tvAlert.setText(mFormat.format(value * mPortionSize) + "克");
		}
		tvValue.setText(mFormat.format(value));
	}
}
