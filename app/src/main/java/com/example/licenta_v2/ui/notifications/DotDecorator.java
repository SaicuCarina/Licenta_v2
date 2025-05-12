package com.example.licenta_v2.ui.notifications;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.List;

public class DotDecorator implements DayViewDecorator {
    private final List<CalendarDay> dates;
    private final int color;
    private final Drawable highlightDrawable;

    public DotDecorator(List<CalendarDay> dates, int color) {
        this.dates = dates;
        this.color = color;
        this.highlightDrawable = new ColorDrawable(Color.TRANSPARENT);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
        view.addSpan(new DotSpan(10, color));
    }
}

