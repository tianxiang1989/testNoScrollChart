package test.testNoScrollChart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class noScrollBarChart extends View {
	/** 打印log信息时的标识 */
	private final String TAG = noScrollBarChart.class.getSimpleName();

	// ===============各种常量===============
	/**枚举：标识传来的x数据中最大值和最小值的正负的
	 * BOTH_POSITIVE:最大值最小值均为正数
	 * BOTH_NEGATIVE:最大值最小值均为负数
	 * POSITIVE_NEGATIVE:最大值为正数，最小值为负数
	 * */
	public enum POSITIVE_FLAG {
		BOTH_POSITIVE, BOTH_NEGATIVE, POSITIVE_NEGATIVE
	}

	/**getBaseLine中用到的标识 垂直居中*/
	private final int TEXT_ALIGN_CENTER = 0;
	/**getBaseLine中用到的标识 垂直靠下*/
	private final int TEXT_ALIGN_BOTTOM = 1;
	/**y坐标几等分*/
	private final int Y_HOW_MANY = 6;
	/**y的上边距起始位置*/
	private float MARGIN_TOP = changeDp(0);
	/**标题的高度*/
	private float TITIL_HEIGHT = changeDp(45);
	// ===============view状态变量===============
	/**底边距*/
	private int marginBottom;
	/**view的宽度*/
	float viewWidth;
	/**view的高度*/
	float viewHeight;
	/**图表左端点*/
	float chart_Left_StartX;
	/**y=0的位置 */
	private float zeroYAxis = 0;
	// ===============各种标识变量===============
	/**标识传来的x数据中最大值和最小值的正负*/
	POSITIVE_FLAG positive_Flag;
	/** 是否已经加载完服务器传来的数据--控制是否调用onDraw方法 */
	private boolean runDraw = false;
	/**标识x轴有几个值*/
	private int size_X;
	// ===============view数据===============
	/** chart标题名称 */
	private String chartTitleName;

	/** y轴的数据 */
	private List<Float> yValueList = new ArrayList<Float>();
	/** y轴坐标 [左上角的点] */
	private List<Float> yAxisList = new ArrayList<Float>();

	/** x轴的数据 */
	private List<String> xValueList = new ArrayList<String>();
	/** x轴坐标 [左上角的点] */
	private List<Float> xAxisList = new ArrayList<Float>();

	/** y轴坐标 分割线的y坐标值 */
	private List<Float> yAxisLineList = new ArrayList<Float>();
	/** y轴坐标 分割线的y显示值 */
	private List<String> yAxisLineValueList = new ArrayList<String>();
	// ===============各种画笔===============
	/**背景的画笔*/
	private Paint backGroundPaint;
	/**轴的画笔*/
	private Paint axisPaint;
	/**画x轴数值的textPaint画笔*/
	TextPaint textXYPaint;
	/**阴影的画笔*/
	private Paint shadowPaint;
	/**画标题的画笔*/
	private Paint titlePaint;
	/**背景参考柱状图的画笔*/
	private Paint relativeBarchartPaint;
	/**折线/柱状图文字的画笔*/
	private TextPaint textChartPaint;
	/**柱状图画笔*/
	private Paint barchartPaint;
	/**画y=0横线的画笔*/
	private Paint zeroYPaint;
	/**画y=0数值的画笔*/
	private TextPaint zeroYTextPaint;

	// ---------------各种方法BEGIN---------------
	// ---------------复写父类的方法---------------
	/**
	 * 读取xml中设置的属性
	 * @param context
	 * @param attrs
	 */
	public noScrollBarChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint(); // 初始化画笔
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (runDraw) {// 数据已加载完毕
			super.onDraw(canvas);
			initData();// 初始化数据
			drawOutsideBackground(canvas);// 画背景
			drawYBackgroud(canvas); // 画柱状图的参考背景
			drawTitleText(canvas); // 画标题
			drawYRelativeAxis(canvas);// 画水平的分割线
			drawXZeroLine(canvas);// 画x=0的线
			drawXAxisValue(canvas);// 画x轴上显示的数据
			drawYText(canvas);// 画y轴显示的值
			drawChart(canvas);// 画报表图
		}
	}

	// ---------------初始化数据的方法---------------
	/** 初始化画笔 */
	public void initPaint() {
		// 背景的画笔
		backGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backGroundPaint.setAntiAlias(true);
		backGroundPaint.setColor(Color.WHITE);

		// 轴的画笔
		axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		axisPaint.setColor(0xffc6c6c6);
		axisPaint.setTextSize(changeDp(12));

		// 画x,y值数值的画笔
		textXYPaint = new TextPaint();
		textXYPaint.setTextSize(changeDp(12));
		textXYPaint.setColor(0xffc3c3c3);

		// 背景参考柱状图的画笔
		shadowPaint = new Paint();
		shadowPaint.setAntiAlias(true);
		shadowPaint.setStyle(Paint.Style.FILL);

		// 报表文字的画笔
		textChartPaint = new TextPaint();
		textChartPaint.setTextSize(changeDp(12));
		textChartPaint.setColor(0xff000000);

		// 背景参考柱状图的画笔
		relativeBarchartPaint = new Paint();
		relativeBarchartPaint.setColor(0xfff6f6f6);// 灰色的参考柱状图背景色
		// relativeBarchartPaint.setColor(0xfff0f7fd);// 淡蓝色的参考柱状图背景色
		relativeBarchartPaint.setAntiAlias(true);
		relativeBarchartPaint.setStyle(Paint.Style.FILL);

		// 画y=0横线的画笔
		zeroYPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		zeroYPaint.setAntiAlias(true);
		zeroYPaint.setColor(Color.BLACK);
		zeroYPaint.setAlpha(95);
		// zeroXPaint.setStrokeWidth(changeDp(2));

		// 画y=0数值的画笔
		zeroYTextPaint = new TextPaint();
		zeroYTextPaint.setTextSize(changeDp(16));
		zeroYTextPaint.setColor(Color.RED);

		// 画标题的画笔
		titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setAntiAlias(true);
		titlePaint.setColor(0xff727272);
		// titlePaint.setAlpha(90);//透明度
		titlePaint.setTextSize(changeDp(14));

		// 柱状图画笔
		barchartPaint = new Paint();
		barchartPaint.setColor(0xff70cbf7);// 蓝色的柱状图
		barchartPaint.setAntiAlias(true);
		barchartPaint.setStyle(Paint.Style.FILL);
	}

	private void initData() {
		// 1 变量赋值
		size_X = xValueList.size();
		viewWidth = this.getWidth();
		viewHeight = this.getHeight();
		// 2 计算的数据
		calYRelativeAxis();
		// 3 计算x的数值
		calXAxisList();
	}

	// ---------------计算数值的方法---------------

	/**计算y分割线的数据*/
	private void calYRelativeAxis() {
		if (calXNameMaxLeng() > 4) {// TODO_Q 这里只考虑了一行两行的情况 [因为实际x名称数据最多8位]
			marginBottom = (int) ((getFontHeight(textXYPaint) + changeDp(2)) * 2) + changeDp(2);
		} else {
			marginBottom = (int) ((getFontHeight(textXYPaint) + changeDp(2))) + changeDp(2);
		}

		// 变量列表
		/**y值的最小值*/
		float yMinValue = getYMinValue();
		/**y值的最大值*/
		float yMaxValue = getYMaxValue();
		/**精度比例*/
		float degree;
		/**画的y最大值的位置*/
		float yDrawMaxValue;
		/**画的y最小值的位置*/
		float yDrawMinValue;
		/**传来数组中y的值*/
		float yValue;
		/**传来数组中y的坐标*/
		float yAlis;
		/**y坐标的间距*/
		float perSpaceAxisValue = (viewHeight - (marginBottom) - (MARGIN_TOP + TITIL_HEIGHT))
				/ Y_HOW_MANY;
		// 方法列表
		judgPositive(yMinValue, yMaxValue);// 判断最大最小值的正负

		switch (positive_Flag) {
		case BOTH_POSITIVE:// 最大最小值均为正值
			// 思路：从x的最小值计算各个点
			if ((yMaxValue - yMinValue) / 2f > yMinValue) {
				yDrawMinValue = yMinValue / 1.3f;
			} else {
				yDrawMinValue = 0;
			}
			degree = (yMaxValue - yDrawMinValue)
					/ (viewHeight - marginBottom - (MARGIN_TOP + TITIL_HEIGHT)
							- getFontHeight(axisPaint) - changeDp(4));

			// 计算y各个值的坐标
			yAxisList.clear();
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = viewHeight - marginBottom - (yValue - yDrawMinValue) / degree;
				yAxisList.add(yAlis);
			}

			// 计算y等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMinValue));
				yDrawMinValue = yDrawMinValue + perSpaceAxisValue * degree;
			}
			Collections.reverse(yAxisLineValueList);// list翻转
			break;
		case BOTH_NEGATIVE:// 最大值最小值均为负值
			// 思路:从y的最大值计算各个点
			if ((yMinValue - yMaxValue) / 2f < yMaxValue) {
				yDrawMaxValue = 0;
			} else {
				yDrawMaxValue = yMaxValue - yMaxValue * 0.01f;
			}
			degree = (yDrawMaxValue - yMinValue)
					/ (viewHeight - (marginBottom) - (MARGIN_TOP + TITIL_HEIGHT)
							- getFontHeight(axisPaint) - changeDp(4));// 总高-下高-上高-表报上方文字的高度
			// 计算y各个值的坐标
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = MARGIN_TOP + TITIL_HEIGHT + (yDrawMaxValue - yValue) / degree;
				yAxisList.add(yAlis);
			}

			// 计算y等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMaxValue));
				yDrawMaxValue = yDrawMaxValue - perSpaceAxisValue * degree;
			}
			Collections.reverse(yAxisLineValueList);// list翻转

			break;
		case POSITIVE_NEGATIVE:// 最大值为正值，最小值为负值
			// TODO_Q 正负的y值计算以及绘制图形

			// 思路：求出最上边的线，然后计算出各个点
			degree = (yMaxValue - yMinValue)
					/ (viewHeight - (marginBottom) - (MARGIN_TOP + TITIL_HEIGHT) - (getFontHeight(axisPaint) + changeDp(3)) * 2f);//
			// (最大值-最小值)/(总高-上高-下高-文字高度*2)
			yDrawMaxValue = yMaxValue + (getFontHeight(axisPaint) + changeDp(3)) * degree;// 最上边的点
			// 绘制的y最大值坐标
			float yMaxAxis = MARGIN_TOP + TITIL_HEIGHT;
			zeroYAxis = yDrawMaxValue / degree + yMaxAxis;// 计算x=0的点的坐标

			// 计算x各个值的坐标
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = zeroYAxis - (yValue) / degree;
				yAxisList.add(yAlis);
			}
			// 计算x等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMaxValue));
				yDrawMaxValue = yDrawMaxValue - perSpaceAxisValue * degree;
			}
			Collections.reverse(yAxisLineValueList);// list翻转
			break;
		}

		// 计算y分割线的数据
		float degree2 = (viewHeight - (MARGIN_TOP + TITIL_HEIGHT) - (marginBottom)) / Y_HOW_MANY;
		yAxisLineList.clear();
		float localBeginY = MARGIN_TOP + TITIL_HEIGHT;
		for (int i = 0; i < Y_HOW_MANY; i++) {
			yAxisLineList.add(localBeginY);
			localBeginY = localBeginY + degree2;
		}
		yAxisLineList.add(viewHeight - (marginBottom));// 最下边的线 (因为float的除法会有误差)
		Collections.reverse(yAxisLineList);// list翻转
	}

	/**x轴名称的最大长度*/
	private int calXNameMaxLeng() {
		int maxLength = 0;
		for (String x : xValueList) {
			int l = x.length();
			if (l > maxLength) {
				maxLength = l;
			}
		}
		return maxLength;
	}

	/**判断最大最小值的正负*/
	private void judgPositive(float xMinValue, float xMaxValue) {
		if (xMinValue >= 0) {// 最大最小值均为正值
			positive_Flag = POSITIVE_FLAG.BOTH_POSITIVE;
		} else if (xMaxValue < 0) {// 最大值最小值均为负值
			positive_Flag = POSITIVE_FLAG.BOTH_NEGATIVE;
		} else if (xMaxValue >= 0 && xMinValue < 0) {// 最大值为正值，最小值为负值
			positive_Flag = POSITIVE_FLAG.POSITIVE_NEGATIVE;
		}
	}

	/**获得y轴的最小值*/
	private float getYMinValue() {
		float localYMinValue = yValueList.size() == 0 ? 0 : yValueList.get(yValueList.size() - 1);
		// Log.v(TAG, "localXMinValue==" + localXMinValue);
		for (float m : yValueList) {
			if (m < localYMinValue) {
				localYMinValue = m;
			}
		}
		return localYMinValue;
	}

	/**获得y轴的最大值*/
	private float getYMaxValue() {
		float localYMaxValue = yValueList.size() == 0 ? 0 : yValueList.get(0);
		// Log.v(TAG, "localXMaxValue==" + localXMaxValue);
		for (float m : yValueList) {
			if (m > localYMaxValue) {
				localYMaxValue = m;
			}
		}
		return localYMaxValue;
	}

	/**计算y轴上显示的值的最大长度*/
	private float calMaxYAxisLineValueLength() {
		float maxW = changeDp(30);// 保存最小宽度 从30dp开始
		for (String y : yAxisLineValueList) {
			float valueWidth = textXYPaint.measureText(y);
			if (valueWidth > maxW) {
				maxW = valueWidth;
			}
		}
		maxW = maxW + changeDp(12);// 文字两边留间距
		return maxW;
	}

	/**计算x相关的数据*/
	private void calXAxisList() {
		chart_Left_StartX = calMaxYAxisLineValueLength();// 报表起始位置
		float yAxisValueLength = textChartPaint.measureText((xValueList.size() > 0) ? ""
				: xValueList.get(xValueList.size() - 1));
		float ySpacing = (viewWidth - chart_Left_StartX - yAxisValueLength / 2) / size_X;// y的间距
		xAxisList.clear();
		float startX = chart_Left_StartX;
		for (int i = 0; i < xValueList.size(); i++) {
			xAxisList.add(startX + ySpacing / 2F);
			startX = startX + ySpacing;
		}
		Log.v(TAG, "calXAxisList() xAxisList==" + xAxisList);
	}

	// ---------------控制状态的方法---------------
	/**数据就位，开始画图(调用onDraw方法)*/
	public void startRunDraw() {
		runDraw = true;
		postInvalidate();
	}
	// ---------------画图的方法---------------
	/**画最外面的背景*/
	private void drawOutsideBackground(Canvas canvas) {
		float rectLeft = 0;
		float rectTop = MARGIN_TOP;
		float rectRight = this.viewWidth;
		float rectBottom = this.viewHeight;
		RectF backGroundRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
		canvas.drawRoundRect(backGroundRect, 0, 0, backGroundPaint);
	}

	/**画参考柱状图的背景色*/
	private void drawYBackgroud(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			RectF bar = new RectF(xAxisList.get(i) - changeDp(9), MARGIN_TOP + TITIL_HEIGHT,
					xAxisList.get(i) + changeDp(9), viewHeight - marginBottom);
			canvas.drawRect(bar, relativeBarchartPaint);
		}
	}

	/**画标题*/
	private void drawTitleText(Canvas canvas) {
		float rectLeft = changeDp(5);
		float rectTop = MARGIN_TOP;
		float rectRight = this.viewWidth - changeDp(5);
		float rectBottom = TITIL_HEIGHT;
		RectF textRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
		float baseline = getBaseLine(titlePaint, textRect, TEXT_ALIGN_CENTER);// 纵向居中
		titlePaint.setTextAlign(Paint.Align.CENTER);// 水平居中
		canvas.drawText(chartTitleName, textRect.centerX(), baseline, titlePaint);
	}

	/**画水平的分割线*/
	private void drawYRelativeAxis(Canvas canvas) {
		for (int i = 0; i < yAxisLineList.size(); i++) {
			canvas.drawLine(chart_Left_StartX, yAxisLineList.get(i), viewWidth - changeDp(15),
					yAxisLineList.get(i), axisPaint);
		}
	}

	/**画x=0的线*/
	private void drawXZeroLine(Canvas canvas) {
		canvas.drawLine(chart_Left_StartX, MARGIN_TOP + TITIL_HEIGHT - changeDp(5),
				chart_Left_StartX, viewHeight - marginBottom, axisPaint);
	}

	/**画x轴上显示的值*/
	private void drawXAxisValue(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			// 文字
			String str = xValueList.get(i) + "";
			StaticLayout layout = null;
			int lastLenth = (int) textXYPaint.measureText(str);

			if (str.length() > 4) {// 一行四个字
				lastLenth = (int) (lastLenth / 1.5);
			}
			layout = new StaticLayout(str, textXYPaint, lastLenth, Alignment.ALIGN_CENTER, 1.0F,
					0.0F, true);

			int cur = canvas.save(); // 保存当前状态
			if (str.length() > 4) {// 一行四个字
				canvas.translate(xAxisList.get(i) - layout.getLineWidth(0) / 2, viewHeight
						- (marginBottom) * 1f + changeDp(2));// 画笔的位置
			} else {
				canvas.translate(xAxisList.get(i) - layout.getLineWidth(0) / 2, viewHeight
						- (marginBottom) * 1f);// 画笔的位置
			}

			layout.draw(canvas);
			canvas.restoreToCount(cur);
		}
	}

	/**画Y轴上显示的值*/
	private void drawYText(Canvas canvas) {
		// 画y轴数值
		for (int i = 0; i < yAxisLineList.size(); i++) {
			float yAxis = yAxisLineList.get(i);
			String str = yAxisLineValueList.get(i);
			canvas.drawText(str, chart_Left_StartX / 2 - textXYPaint.measureText(str) / 2f, yAxis
					+ getFontHeight(textXYPaint) / 2f, textXYPaint);
		}
	}

	/**
	 * 画柱状图 
	 * @param canvas
	 */
	private void drawChart(Canvas canvas) {
		switch (positive_Flag) {
		case BOTH_POSITIVE:
			for (int i = 0; i < xValueList.size(); i++) {
				// 图形
				RectF bar = null;
				bar = new RectF(xAxisList.get(i) - changeDp(9), yAxisList.get(i), xAxisList.get(i)
						+ changeDp(9), viewHeight - marginBottom);
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = yValueList.get(i) + "";
				StaticLayout layout = new StaticLayout(str, textChartPaint,
						(int) textChartPaint.measureText(str), Alignment.ALIGN_NORMAL, 1.0F, 0.0F,
						true);
				int cur = canvas.save(); // 保存当前状态
				canvas.translate(xAxisList.get(i) - textChartPaint.measureText(str) / 2f,
						yAxisList.get(i) - getFontHeight(textChartPaint) - changeDp(3));// 画笔的位置
				layout.draw(canvas);
				canvas.restoreToCount(cur);
			}
			break;
		case BOTH_NEGATIVE:
			for (int i = 0; i < xValueList.size(); i++) {
				// 图形
				RectF bar = null;
				bar = new RectF(xAxisList.get(i) - changeDp(9), MARGIN_TOP + TITIL_HEIGHT,
						xAxisList.get(i) + changeDp(9), yAxisList.get(i));
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = yValueList.get(i) + "";
				float textWidth = textChartPaint.measureText(str);
				StaticLayout layout = new StaticLayout(str, textChartPaint, (int) textWidth,
						Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, true);
				int cur = canvas.save(); // 保存当前状态
				canvas.translate(xAxisList.get(i) - textChartPaint.measureText(str) / 2f,
						yAxisList.get(i));// 画笔的位置
				layout.draw(canvas);
				canvas.restoreToCount(cur);
			}
			break;
		case POSITIVE_NEGATIVE:// 最大值为正，最小值为负
			for (int i = 0; i < yValueList.size(); i++) {
				// 图形
				RectF bar = null;
				if (zeroYAxis < yAxisList.get(i)) {
					bar = new RectF(xAxisList.get(i) - changeDp(9), yAxisList.get(i),
							xAxisList.get(i) + changeDp(9), zeroYAxis);
				} else {
					bar = new RectF(xAxisList.get(i) - changeDp(9), zeroYAxis, xAxisList.get(i)
							+ changeDp(9), yAxisList.get(i));
				}
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = yValueList.get(i) + "";
				float textWidth = textChartPaint.measureText(str);
				if (str.indexOf("-") > -1) {// 负数
					StaticLayout layout = new StaticLayout(str, textChartPaint, (int) textWidth,
							Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, true);
					int cur = canvas.save(); // 保存当前状态
					canvas.translate(xAxisList.get(i) - textChartPaint.measureText(str) / 2f,
							yAxisList.get(i));// 画笔的位置
					layout.draw(canvas);
					canvas.restoreToCount(cur);
				} else {// 正数
					StaticLayout layout = new StaticLayout(str, textChartPaint,
							(int) textChartPaint.measureText(str), Alignment.ALIGN_NORMAL, 1.0F,
							0.0F, true);
					int cur = canvas.save(); // 保存当前状态
					canvas.translate(xAxisList.get(i) - textChartPaint.measureText(str) / 2f,
							yAxisList.get(i) - getFontHeight(textChartPaint) - changeDp(3));// 画笔的位置
					layout.draw(canvas);
					canvas.restoreToCount(cur);
				}
				// y=0的线和y=0的文字
				canvas.drawLine(chart_Left_StartX, zeroYAxis, viewWidth - changeDp(15), zeroYAxis,
						zeroYPaint);// y=0的线

				canvas.drawText("0", chart_Left_StartX + changeDp(2), zeroYAxis
						+ getFontHeight(zeroYTextPaint) - changeDp(2), zeroYTextPaint);// y=0的文字
			}
			break;
		default:
			break;
		}
	}

	// ---------------工具方法---------------
	/**
	 * dp转化px像素 [工具方法]
	 * @param dp
	 * @return px
	 */
	private int changeDp(int dp) {
		int pix = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics()));
		return pix;
	}

	/**
	 * 根据paint计算文字高度 [工具方法]
	 * 
	 * @param paint 画笔
	 * @return 计算得出的文字高度
	 */
	public float getFontHeight(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		float fFontHeight = (float) Math.ceil(fm.descent - fm.ascent);
		return fFontHeight;
	}

	/**
	 * 数字格式化[工具方法]
	 * @param ff 需要处理的数据
	 * @return
	 */
	private String formatNum(float ff) {
		DecimalFormat df;
		String res = "";
		int positiveOrNegativeFlag = 1;// 标识是正数还是负数
		if (ff < 0) {// 先转正数做判断
			positiveOrNegativeFlag = -1;
			ff = -ff;
		}

		if (ff == 0) {
			res = "0";
		} else if (ff < 1) {
			df = new DecimalFormat("###.000");
			res = "0" + df.format(ff) + "";
		} else if (ff > 100) {
			res = (int) ff + "";
		} else {
			df = new DecimalFormat("###.00");
			res = df.format(ff) + "";
		}
		if (positiveOrNegativeFlag == -1) {
			res = "-" + res;
		}
		return res;
	}

	/**
	 * 取需要画的文字的相对y轴的位置 [工具方法]
	 * 
	 * @param paint 画笔
	 * @param targetRect 文字的rect位置
	 * @param type 标识垂直对齐的方式
	 * @return
	 */
	private float getBaseLine(Paint paint, RectF targetRect, int type) {
		float baseline;
		Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		switch (type) {
		case TEXT_ALIGN_CENTER:// 居中
			baseline = targetRect.top
					+ (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top)
					/ 2 - fontMetrics.top;
			break;
		case TEXT_ALIGN_BOTTOM:// 底边对齐
			baseline = targetRect.top + ((fontMetrics.bottom - fontMetrics.top) / 2);
			break;
		default:
			baseline = 0;
			break;
		}
		return baseline;
	}

	// ===============对外的set和get方法===============

	public void setyValueList(List<Float> yValueList) {
		this.yValueList = yValueList;
	}

	public void setyAxisList(List<Float> yAxisList) {
		this.yAxisList = yAxisList;
	}

	public void setxValueList(List<String> xValueList) {
		this.xValueList = xValueList;
	}

	public void setxAxisList(List<Float> xAxisList) {
		this.xAxisList = xAxisList;
	}

	public void setyAxisLineList(List<Float> yAxisLineList) {
		this.yAxisLineList = yAxisLineList;
	}

	public void setyAxisLineValueList(List<String> yAxisLineValueList) {
		this.yAxisLineValueList = yAxisLineValueList;
	}

	public void setChartTitleName(String chartTitleName) {
		this.chartTitleName = chartTitleName;
	}

}
