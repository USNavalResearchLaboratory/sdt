package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.ToolTipRenderer;

public class SdtPath extends Path 
{

	private String toolTipText;

	private Font toolTipFont = null;

	private boolean showToolTip = false;

	private java.awt.Color textColor = Color.BLACK;


	private Font makeToolTipFont()
	{
		HashMap<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>();

		fontAttributes.put(TextAttribute.BACKGROUND, new java.awt.Color(0.4f, 0.4f, 0.4f, 1f));
		return Font.decode("Arial-BOLD-14").deriveFont(fontAttributes);
	}


	private void addToolTip(DrawContext dc, Vec4 linePoint)
	{
		if (getToolTipFont() == null && getToolTipText() == null)
			return;

		final Vec4 screenPoint = dc.getView().project(linePoint);
		if (screenPoint == null)
			return;

		OrderedText tip = new OrderedText(getToolTipText(), getToolTipFont(), screenPoint,
			getToolTipTextColor(), 0d);
		dc.addOrderedRenderable(tip);
	}

	private class OrderedText implements OrderedRenderable
	{
		Font font;

		String text;

		Vec4 point;

		double eyeDistance;

		java.awt.Color color;


		OrderedText(String text, Font font, Vec4 point, java.awt.Color color, double eyeDistance)
		{
			this.text = text;
			this.font = font;
			this.point = point;
			this.eyeDistance = eyeDistance;
			this.color = color;
		}


		@Override
		public double getDistanceFromEye()
		{
			return this.eyeDistance;
		}


		@Override
		public void render(DrawContext dc)
		{
			ToolTipRenderer toolTipRenderer = this.getToolTipRenderer(dc);
			if (this.text != null)
				toolTipRenderer.render(dc, this.text, (int) this.point.x, (int) this.point.y);
		}


		@Override
		public void pick(DrawContext dc, java.awt.Point pickPoint)
		{
		}


		protected ToolTipRenderer getToolTipRenderer(DrawContext dc)
		{
			ToolTipRenderer tr = (this.font != null) ? new ToolTipRenderer(this.font) : new ToolTipRenderer();

			if (this.color != null)
			{
				tr.setTextColor(this.color);
				tr.setOutlineColor(this.color);
				tr.setInteriorColor(ToolTipRenderer.getContrastingColor(this.color));
			}
			else
			{
				tr.setUseSystemLookAndFeel(true);
			}

			return tr;
		}
	}


	@Override
	public void render(DrawContext dc)
	{
		// Determine Cartesian position from the surface geometry if the icon is near the surface,
		// otherwise draw it from the globe.
		Position pos = getReferencePosition();
		Vec4 iconPoint = null;
		if (pos.getElevation() < dc.getGlobe().getMaxElevation())
			iconPoint = dc.getSurfaceGeometry().getSurfacePoint(getReferencePosition());
		if (iconPoint == null)
			iconPoint = dc.getGlobe().computePointFromPosition(getReferencePosition());
		if (isShowToolTip())
			this.addToolTip(dc, iconPoint);

		super.render(dc);
	}


	public String getToolTipText()
	{
		return toolTipText;
	}


	public void setToolTipText(String toolTipText)
	{
		this.toolTipText = toolTipText;
	}


	public Font getToolTipFont()
	{
		if (this.toolTipFont == null)
		{
			this.toolTipFont = this.makeToolTipFont();
		}
		return toolTipFont;
	}


	public void setToolTipFont(Font toolTipFont)
	{
		this.toolTipFont = toolTipFont;
	}


	public boolean isShowToolTip()
	{
		return showToolTip;
	}


	public void setShowToolTip(boolean showToolTip)
	{
		this.showToolTip = showToolTip;
	}


	public Color getToolTipTextColor()
	{
		return textColor;
	}


	public void setToolTipTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

}
