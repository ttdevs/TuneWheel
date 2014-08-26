package com.ttdevs.wheel.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.ttdevs.wheel.R;

/**
 * 卷尺控件类。由于时间比较紧，只有下班后有时间，因此只实现了基本功能。<br>
 * 目前存在的问题是滑动过程中数字没有跟随变化，加速滑动未实现。导致这个问题的原因是时间仓促可能需要重新设置数值更新的思路并同时解决支持更多单位的问题。<br>
 * 
 * 周末有时间会继续更新<br>
 * 
 * @author ttdevs
 * @version create：2014年8月26日
 */
@SuppressLint("ClickableViewAccessibility")
public class TuneWheel extends View {

	public interface OnValueChangeListener {
		public void onValueChange(float value);
	}

	public static final int MOD_TYPE_HALF = 2;
	public static final int MOD_TYPE_ONE = 10;

	private static final int ITEM_HALF_DIVIDER = 40;
	private static final int ITEM_ONE_DIVIDER = 10;

	private static final int ITEM_MAX_HEIGHT = 50;
	private static final int ITEM_MIN_HEIGHT = 20;

	private static final int TEXT_SIZE = 18;

	private float mDensity;
	private int mValue = 50, mMaxValue = 100, mModType = MOD_TYPE_HALF, mLineDivider = ITEM_HALF_DIVIDER;
	// private int mValue = 50, mMaxValue = 500, mModType = MOD_TYPE_ONE,
	// mLineDivider = ITEM_ONE_DIVIDER;

	private int mLastX, mMove;
	private int mWidth, mHeight;

	private int mMinVelocity, mMidValue, mMiddleValue;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private OnValueChangeListener mListener;

	public TuneWheel(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScroller = new Scroller(getContext());
		mDensity = getContext().getResources().getDisplayMetrics().density;

		mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

		setBackgroundResource(R.drawable.bg_wheel);
	}

	/**
	 * 
	 * 考虑可扩展，但是时间紧迫，只可以支持两种类型效果图中两种类型
	 * 
	 * @param value
	 *            初始值
	 * @param maxValue
	 *            最大值
	 * @param model
	 *            刻度盘精度：<br>
	 *            {@link MOD_TYPE_HALF}<br>
	 *            {@link MOD_TYPE_ONE}<br>
	 */
	public void initViewParam(int defaultValue, int maxValue, int model) {
		switch (model) {
		case MOD_TYPE_HALF:
			mModType = MOD_TYPE_HALF;
			mLineDivider = ITEM_HALF_DIVIDER;
			mValue = defaultValue * 2;
			mMaxValue = maxValue * 2;
			break;
		case MOD_TYPE_ONE:
			mModType = MOD_TYPE_ONE;
			mLineDivider = ITEM_ONE_DIVIDER;
			mValue = defaultValue;
			mMaxValue = maxValue;
			break;

		default:
			break;
		}
		invalidate();

		mLastX = 0;
		mMove = 0;
		notifyValueChange();
	}

	/**
	 * 设置用于接收结果的监听器
	 * 
	 * @param listener
	 */
	public void setValueChangeListener(OnValueChangeListener listener) {
		mListener = listener;
	}

	/**
	 * 获取当前刻度值
	 * 
	 * @return
	 */
	public float getValue() {
		return mValue;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mWidth = getWidth();
		mHeight = getHeight();
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawScaleLine(canvas);
		drawMiddleLine(canvas);
	}

	private void drawScaleLine(Canvas canvas) {
		canvas.save();

		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(2);
		linePaint.setColor(Color.BLACK);

		TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(TEXT_SIZE * mDensity);

		int width = mWidth, drawCount = 0;
		float xPosition = 0, textWidth = Layout.getDesiredWidth("0", textPaint);

		for (int i = 0; drawCount <= 2 * width; i++) {
			int numSize = String.valueOf(mValue + i).length();

			xPosition = (width / 2 - mMove) + i * mLineDivider * mDensity;
			if ((mValue + i) % mModType == 0) {
				canvas.drawLine(xPosition, 0, xPosition, mDensity * ITEM_MAX_HEIGHT, linePaint);

				if (mValue + i <= mMaxValue) {
					switch (mModType) {
					case MOD_TYPE_HALF:
						canvas.drawText(String.valueOf((mValue + i) / 2),
								countLeftStart(mValue + i, xPosition, textWidth), getHeight() - textWidth, textPaint);
						break;
					case MOD_TYPE_ONE:
						canvas.drawText(String.valueOf(mValue + i), xPosition - (textWidth * numSize / 2), getHeight()
								- textWidth, textPaint);
						break;

					default:
						break;
					}
				}
			} else {
				canvas.drawLine(xPosition, 0, xPosition, mDensity * ITEM_MIN_HEIGHT, linePaint);
			}

			xPosition = (width / 2 - mMove) - i * mLineDivider * mDensity;
			if ((mValue - i) % mModType == 0) {
				canvas.drawLine(xPosition, 0, xPosition, mDensity * ITEM_MAX_HEIGHT, linePaint);

				if (mValue - i >= 0) {
					switch (mModType) {
					case MOD_TYPE_HALF:
						canvas.drawText(String.valueOf((mValue - i) / 2),
								countLeftStart(mValue - i, xPosition, textWidth), getHeight() - textWidth, textPaint);
						break;
					case MOD_TYPE_ONE:
						canvas.drawText(String.valueOf(mValue - i), xPosition - (textWidth * numSize / 2), getHeight()
								- textWidth, textPaint);
						break;

					default:
						break;
					}
				}
			} else {
				canvas.drawLine(xPosition, 0, xPosition, mDensity * ITEM_MIN_HEIGHT, linePaint);
			}
			drawCount += 2 * mLineDivider * mDensity;
		}

		canvas.restore();
	}

	/**
	 * 计算没有数字显示位置的辅助方法
	 * 
	 * @param value
	 * @param xPosition
	 * @param textWidth
	 * @return
	 */
	private float countLeftStart(int value, float xPosition, float textWidth) {
		float xp = 0f;
		if (value < 20) {
			xp = xPosition - (textWidth * 1 / 2);
		} else {
			xp = xPosition - (textWidth * 2 / 2);
		}
		return xp;
	}

	/**
	 * 画中间的红色指示线、阴影等。指示线两端简单的用了两个矩形代替
	 * 
	 * @param canvas
	 */
	private void drawMiddleLine(Canvas canvas) {
		int gap = 12, indexWidth = 8, indexTitleWidth = 24, indexTitleHight = 10, shadow = 6; // TODO
																								// gap
		String color = "#66999999";

		canvas.save();

		Paint redPaint = new Paint();
		redPaint.setStrokeWidth(indexWidth);
		redPaint.setColor(Color.RED);
		canvas.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, redPaint);

		Paint ovalPaint = new Paint();
		ovalPaint.setColor(Color.RED);
		ovalPaint.setStrokeWidth(indexTitleWidth);
		canvas.drawLine(mWidth / 2, 0, mWidth / 2, indexTitleHight, ovalPaint);
		canvas.drawLine(mWidth / 2, mHeight - indexTitleHight, mWidth / 2, mHeight, ovalPaint);

		// RectF ovalRectF = new RectF(mWidth / 2 - 10, 0, mWidth / 2 + 10, 4 *
		// mDensity); //TODO
		// canvas.drawOval(ovalRectF, ovalPaint);
		// ovalRectF.set(mWidth / 2 - 10, mHeight - 8 * mDensity, mWidth / 2 +
		// 10, mHeight); //TODO

		Paint shadowPaint = new Paint();
		shadowPaint.setStrokeWidth(shadow);
		shadowPaint.setColor(Color.parseColor(color));
		canvas.drawLine(mWidth / 2 + gap, 0, mWidth / 2 + gap, mHeight, shadowPaint);

		canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		int action = event.getAction();
		int xPosition = (int) event.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = xPosition;
			mMove = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			mMove += (mLastX - xPosition);
			postInvalidate();

			break;
		case MotionEvent.ACTION_UP:
			int roundMove = Math.round(mMove / (mLineDivider * mDensity));
			mValue = mValue + roundMove;
			mValue = mValue <= 0 ? 0 : mValue;
			mValue = mValue > mMaxValue ? mMaxValue : mValue;

			notifyValueChange();

			// mVelocityTracker.computeCurrentVelocity(1000);
			// float xVelocity = mVelocityTracker.getXVelocity();
			// if (Math.abs(xVelocity) > mMinVelocity) {
			// mScroller.fling(xPosition, 0, -(int) xVelocity, 0,
			// Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
			// mMidValue = 0;
			// }
			mLastX = 0;
			mMove = 0;
			return false;
			// break;
		default:
			break;
		}

		mLastX = xPosition;
		return true;
	}

	private void notifyValueChange() {
		if (null != mListener) {
			if (mModType == MOD_TYPE_ONE) {
				mListener.onValueChange(mValue);
			}
			if (mModType == MOD_TYPE_HALF) {
				mListener.onValueChange(mValue / 2f);
			}
		}
	}

	// @Override
	// public void computeScroll() {
	// String str = String.format("Scroller X:%d \t\t\t\t Y:%d",
	// mScroller.getCurrX(), mScroller.getCurrY());
	// String str1 = String.format("Scroller final X:%d \t\t\t\t Y:%d",
	// mScroller.getFinalX(), mScroller.getFinalY());
	// System.out.println(str);
	// System.out.println(str1);
	//
	// if (mScroller.computeScrollOffset()) {
	// if (mScroller.getCurrX() == mScroller.getFinalX()) { // over
	// // mMidValue = Math.round(mMove / (mLineDivider * mDensity));
	// // mValue -= mMidValue;
	// //
	// // int xp = mScroller.getCurrX();
	// // mMove += (xp - mLastX);
	// // mMidValue = Math.round(mMove / (mLineDivider * mDensity));
	// //
	// // mValue += mMidValue;
	// //
	// // mMidValue = 0;
	// // mMove = 0;
	// } else { // scrolling
	// mValue -= mMidValue;
	//
	// int xp = mScroller.getCurrX();
	// mMove += (xp - mLastX);
	//
	// mMidValue = Math.round(mMove / (mLineDivider * mDensity));
	//
	// mValue += mMidValue;
	//
	// mLastX = xp;
	// }
	// postInvalidate();
	// }
	// }
}
