package com.kedzie.vbox.metrics;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kedzie.vbox.PreferencesActivity;
import com.kedzie.vbox.R;
import com.kedzie.vbox.Utils;
import com.kedzie.vbox.VBoxApplication;

public class MetricRenderer extends View {
	private static String TAG = MetricRenderer.class.getSimpleName();
	
	/** Maximum Y Value */
	protected int _max;
	/** # of data points */
	protected int _count;
	/** Time interval between datapoints */
	protected int _period;
	/** view size (pixels) */
	protected int _width, _height;
	/** Metric names to render */
	protected String[] _metrics;
	/** pixels/period */
	protected int hStep;
	/** pixels/unit */
	protected double vStep;
	/** Metric data */
	protected Map<String, MetricQuery> _data = new HashMap<String, MetricQuery>();
	
	private Rect bounds = new Rect();
	private Paint textPaint = new Paint(), bgPaint = new Paint(), borderPaint = new Paint(), metricPaint = new Paint(), gridPaint = new Paint(), metricFill=new Paint();
	private Path path = new Path();
	
	{
		bgPaint.setStyle(Style.FILL);
		bgPaint.setColor(getContext().getResources().getColor(R.color.METRIC_BACKGROUND));
		
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setColor(getContext().getResources().getColor(R.color.METRIC_BORDER));
		borderPaint.setAntiAlias(true);
		borderPaint.setStrokeWidth(2.0f);
		
		textPaint.setColor(getContext().getResources().getColor(R.color.METRIC_TEXT));
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(18.0f);
		
		gridPaint.setColor(getContext().getResources().getColor(R.color.METRIC_GRID));
		gridPaint.setAntiAlias(true);
		gridPaint.setStrokeWidth(1.5f);
		
		metricPaint.setStrokeWidth(2.0f);
		metricPaint.setStrokeJoin(Join.MITER);
		metricPaint.setStrokeCap(Cap.ROUND);
		metricPaint.setAntiAlias(true);
		metricPaint.setShadowLayer(4.0f, 2.0f, 2.0f, 0xdd000000);
		
		metricFill.setStyle(Style.FILL);
	}

	public MetricRenderer(Context context) {
		super(context);
	}

	public MetricRenderer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init( int max, String []metrics) {
		_max=max;
		_metrics=metrics;
		_count=Utils.getIntPreference(getContext(), PreferencesActivity.COUNT);
		_period=Utils.getIntPreference(getContext(), PreferencesActivity.PERIOD);
		updateSize();
	}

	public void updateSize() {
		Log.i(TAG, "OnSizeChanged("+getWidth()+"," + getHeight() + ")");
		_width=getWidth();
		_height=getHeight();
		vStep = (float)_height/(float)_max;
		hStep = _width/_count;
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateSize();
	}

	public synchronized void setQuery(Map<String, MetricQuery> q) {
		Log.i(TAG, "Received Metric data");
		_data=q;
		postInvalidate();
	}
	
	@Override
	protected synchronized  void onDraw(Canvas canvas) {
		canvas.getClipBounds(bounds);
		canvas.drawRect(bounds, bgPaint);
		canvas.drawRect(bounds, borderPaint);
		
		int horiz = bounds.right;
		for(int i=0; i<=_count; i+=5) {	//horizontal grid
			canvas.drawLine(horiz, bounds.bottom, horiz, bounds.top, gridPaint);
			canvas.drawText(i*_period+"sec", horiz, bounds.bottom-20, textPaint);
			horiz-=hStep;
		}

		for(String metric : _metrics) {
			if(!_data.containsKey(metric)) continue;
			
			String colorName = metric.replace('/', '_').replace("Guest_","");
			metricPaint.setColor(VBoxApplication.getColor(getContext(), colorName));
			metricFill.setColor(VBoxApplication.getColor(getContext(), colorName+"_Fill"));
			
			int[] data = _data.get(metric).values;
			int prevX=bounds.left, prevY=bounds.bottom-(int)(data[0]*vStep);
			for(int i=1; i<data.length; i++) {
				int x=prevX+hStep, y = bounds.bottom-(int)(data[i]*vStep);
				canvas.drawLine(prevX, prevY, x, y, metricPaint);
				path.reset();
				path.moveTo(prevX, bounds.bottom);
				path.lineTo(prevX, prevY);
				path.lineTo(x, y);
				path.lineTo(x, bounds.bottom);
				path.close();
				canvas.drawPath(path, metricFill);
				prevX=x;prevY=y;
			}
		}
	}
}