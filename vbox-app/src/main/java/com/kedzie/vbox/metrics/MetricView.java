package com.kedzie.vbox.metrics;

import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedzie.vbox.R;
import com.kedzie.vbox.VBoxApplication;

public class MetricView extends LinearLayout {

	private TextView _titleTextView;
	private MetricRenderer _renderer;
	private LinearLayout _metricNames;
	private String _header;
	
    public MetricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public MetricView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MetricView, 0, 0);
        try {
            _header = a.getString(R.styleable.MetricView_header);
            int bgColor = a.getColor(R.styleable.MetricView_backgroundColor, android.R.color.white);
            int gridColor = a.getColor(R.styleable.MetricView_gridColor, android.R.color.black);
            int textColor = a.getColor(R.styleable.MetricView_textColor, android.R.color.black);
            int borderColor = a.getColor(R.styleable.MetricView_borderColor, R.color.blue);
            createView(bgColor, gridColor, textColor, borderColor);
        } finally {
            a.recycle();
        }
    }
	
	public MetricView(Context context, String header) {
	    super(context);
	    _header=header;
	    createView(0xFFFFFFFF, android.R.color.black, android.R.color.black, 0xFF0000FF);
	}
	
	public void createView(int bgColor, int gridColor, int textColor, int borderColor) {
	    setOrientation(LinearLayout.VERTICAL);
	    _titleTextView = new TextView(getContext());
        _titleTextView.setText(_header);
        _titleTextView.setTextColor(android.R.color.primary_text_dark);
        addView(_titleTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        _renderer = new MetricRenderer(getContext(), bgColor, gridColor, textColor, borderColor);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.weight=1;
        addView(_renderer, p);
        _metricNames = new LinearLayout(getContext());
        _metricNames.setOrientation(LinearLayout.HORIZONTAL);
        addView(_metricNames, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	public void init(int max, String []metrics) {
		_renderer.init(max, metrics);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		for(String m : metrics) {
			TextView textView = new TextView(getContext());
			textView.setText(m);
			textView.setTextColor(VBoxApplication.getInstance().getColor(getContext(), m.replace('/', '_')));
			textView.setPadding(0,2,8,0);
			_metricNames.addView(textView, p);
		}
	}
	
	public void setMetricPrefs(int count, int period) {
		_renderer.setMetricPrefs(count, period);
	}

	public void setQueries(Map<String,MetricQuery> q) {
		_renderer.setQuery(q);
	}
	
	public String getHeader() {
	    return _header;
	}
}
