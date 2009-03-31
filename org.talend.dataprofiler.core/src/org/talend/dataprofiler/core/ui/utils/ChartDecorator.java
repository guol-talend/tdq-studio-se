// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.utils;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;

/**
 * DOC bzhou class global comment. Detailled comment
 */
public final class ChartDecorator {

    private static final int BASE_ITEM_LABEL_SIZE = 12;

    private static final int BASE_LABEL_SIZE = 12;

    private static final int BASE_TICK_LABEL_SIZE = 10;

    private static final int BASE_LEGEND_LABEL_SIZE = 10;

    private static final int BASE_TITLE_LABEL_SIZE = 14;

    /**
     * DOC bZhou ChartDecorator constructor comment.
     */
    private ChartDecorator() {
    }

    /**
     * DOC bZhou Comment method "decorate".
     * 
     * @param chart
     */
    public static void decorate(JFreeChart chart) {
        if (chart != null) {
            Plot plot = chart.getPlot();
            if (plot instanceof CategoryPlot) {
                decorateCategoryPlot(chart);

                int rowCount = chart.getCategoryPlot().getDataset().getRowCount();

                for (int i = 0; i < rowCount; i++) {
                    ((CategoryPlot) plot).getRenderer().setSeriesPaint(i, colorList.get(i));
                }

            }

            if (plot instanceof XYPlot) {
                decorateXYPlot(chart);

                int count = chart.getXYPlot().getDataset().getSeriesCount();
                for (int i = 0; i < count; i++) {
                    ((XYPlot) plot).getRenderer().setSeriesPaint(i, colorList.get(i));
                }
            }
        }
    }

    /**
     * DOC bZhou Comment method "decorateCategoryPlot".
     * 
     * @param chart
     */
    private static void decorateCategoryPlot(JFreeChart chart) {

        Font font = null;
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer render = plot.getRenderer();
        CategoryAxis domainAxis = plot.getDomainAxis();
        ValueAxis valueAxis = plot.getRangeAxis();

        font = new Font("Tahoma", Font.BOLD, BASE_ITEM_LABEL_SIZE);

        render.setBaseItemLabelFont(font);

        font = new Font("Verdana", Font.BOLD, BASE_LABEL_SIZE);
        domainAxis.setLabelFont(font);
        valueAxis.setLabelFont(font);

        font = new Font("Verdana", Font.PLAIN, BASE_TICK_LABEL_SIZE);
        domainAxis.setTickLabelFont(font);
        valueAxis.setTickLabelFont(font);

        font = new Font("Tahoma", Font.PLAIN, BASE_LEGEND_LABEL_SIZE);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(font);
        }

        font = new Font("Tahoma", Font.BOLD, BASE_TITLE_LABEL_SIZE);
        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(font);
        }

        font = null;

    }

    /**
     * DOC bZhou Comment method "decorateXYPlot".
     * 
     * @param chart
     */
    private static void decorateXYPlot(JFreeChart chart) {

        Font font = null;
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer render = plot.getRenderer();
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis valueAxis = plot.getRangeAxis();

        font = new Font("Tahoma", Font.BOLD, BASE_ITEM_LABEL_SIZE);

        render.setBaseItemLabelFont(font);

        font = new Font("Verdana", Font.BOLD, BASE_LABEL_SIZE);
        domainAxis.setLabelFont(font);
        valueAxis.setLabelFont(font);

        font = new Font("Verdana", Font.PLAIN, BASE_TICK_LABEL_SIZE);
        domainAxis.setTickLabelFont(font);
        valueAxis.setTickLabelFont(font);

        font = new Font("Tahoma", Font.PLAIN, BASE_LEGEND_LABEL_SIZE);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(font);
        }

        font = new Font("Tahoma", Font.BOLD, BASE_TITLE_LABEL_SIZE);
        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(font);
        }

        font = null;
    }

    private static final Color COLOR_0 = new Color(244, 147, 32);

    private static final Color COLOR_1 = new Color(128, 119, 178);

    private static final Color COLOR_2 = new Color(190, 213, 48);

    private static final Color COLOR_3 = new Color(236, 23, 133);

    private static final Color COLOR_4 = new Color(35, 157, 190);

    private static final Color COLOR_5 = new Color(164, 155, 100);

    private static final Color COLOR_6 = new Color(250, 212, 16);

    private static final Color COLOR_7 = new Color(234, 28, 36);

    private static final Color COLOR_8 = new Color(192, 131, 91);

    private static List<Color> colorList = new ArrayList<Color>();

    static {
        colorList.add(COLOR_7);
        colorList.add(COLOR_0);
        colorList.add(COLOR_1);
        colorList.add(COLOR_2);
        colorList.add(COLOR_3);
        colorList.add(COLOR_4);
        colorList.add(COLOR_5);
        colorList.add(COLOR_6);
        colorList.add(COLOR_8);
    }

    private static Color randomColorPicker() {
        int i = RandomUtils.nextInt(colorList.size());
        return colorList.get(i);
    }
}
