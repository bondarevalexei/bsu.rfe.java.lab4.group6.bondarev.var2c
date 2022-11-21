import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {

    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private final int selectedMarker = -1;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scaleX;
    private double scaleY;

    private double[][] viewport = new double[2][2];
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean rotateLeft = false;

    private Font axisFont;
    private Font labelsFont;

    private BasicStroke axisStroke;
    //private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke gridStroke;
    private BasicStroke selectionStroke;
    private static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();

    private boolean scaleMode = false;
    private boolean showGrid = false;
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, new float[]{4, 1, 1, 1, 2, 1, 1, 1, 4}, 0.0f);
        selectionStroke = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, new float[]{10, 10}, 0.0F);
        gridStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, new float[]{5, 5}, 2.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
        labelsFont = new java.awt.Font("Serif", Font.PLAIN, 10);
    }

    public void showGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;


        this.originalData = new ArrayList<>(graphicsData.size());
        for (Double[] point : graphicsData) {
            Double[] newPoint = new Double[2];
            newPoint[0] = point[0];
            newPoint[1] = point[1];
            this.originalData.add(newPoint);
        }
        this.minX = ((Double[]) graphicsData.get(0))[0];
        this.maxX = ((Double[]) graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[]) graphicsData.get(0))[1];
        this.maxY = this.minY;

        for (int i = 1; i < graphicsData.size(); i++) {
            if (((Double[]) graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[]) graphicsData.get(i))[1];
            }
            if (((Double[]) graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[]) graphicsData.get(i))[1];
            }
        }

        zoomToRegion(minX, maxY, maxX, minY);
    }

    public void zoomToRegion(double minx, double maxy, double maxx, double miny) {
        this.viewport[0][0] = minx;
        this.viewport[0][1] = maxy;
        this.viewport[1][0] = maxx;
        this.viewport[1][1] = miny;
        this.repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - this.minX;
        double deltaY = this.maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    protected void paintGrid(Graphics2D canvas) {
        canvas.setStroke(gridStroke);
        canvas.setColor(Color.GRAY);

        double pos = this.minX;
        double step = (this.maxX - this.minX) / 10;

        while (pos < this.maxX) {
            canvas.draw(new Line2D.Double(xyToPoint(pos, this.maxY), xyToPoint(pos, this.minY)));
            pos += step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(this.maxX, this.maxY), xyToPoint(this.maxX, this.minY)));

        pos = this.minY;
        step = (this.maxY - this.minY) / 10;
        while (pos < this.maxY) {
            canvas.draw(new Line2D.Double(xyToPoint(this.minX, pos), xyToPoint(this.maxX, pos)));
            pos = pos + step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(this.minX, this.maxY), xyToPoint(this.maxX, this.maxY)));
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);

        Double currentX = null;
        Double currentY = null;
        for (Double[] point : this.graphicsData) {
            if ((point[0] >= this.minX) && (point[1] <= this.maxY) &&
                    (point[0] <= this.maxX) && (point[1] >= this.minY)){
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX, currentY),
                            xyToPoint(point[0], point[1])));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(java.awt.Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (!(viewport[0][0] > 0 && viewport[1][0] < 0)) {
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]),
                    xyToPoint(0, viewport[1][1])));
            canvas.draw(new Line2D.Double(xyToPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025,
                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015), xyToPoint(0, viewport[0][1])));
            canvas.draw(new Line2D.Double(xyToPoint((viewport[1][0] - viewport[0][0]) * 0.0025,
                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),
                    xyToPoint(0, viewport[0][1])));
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0.0, viewport[0][1]);
            canvas.drawString("y", (float) labelPos.x + 10, (float) (labelPos.y + bounds.getHeight() / 2));
        }
        if (!(viewport[1][1] > 0.0D && viewport[0][1] < 0.0D)) {
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], 0),
                    xyToPoint(viewport[1][0], 0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0,
                    (viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01,
                    -(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(this.viewport[1][0], 0.0D);
            canvas.drawString("x", (float) (labelPos.x - (bounds.getWidth()/2) - 10), (float) (labelPos.y - bounds.getHeight() / 2));
        }
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        GeneralPath lastMarker = null;
        int i = -1;
        for (Double[] point : graphicsData) {
            i++;

            if (isSpecialPoint(point[1]))
                canvas.setColor(Color.GREEN);
            else
                canvas.setColor(Color.RED);

            // markers
            GeneralPath star = new GeneralPath();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            star.moveTo(center.getX(), center.getY());
            star.lineTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() - 5);
            star.moveTo(star.getCurrentPoint().getX() - 3, star.getCurrentPoint().getY());
            star.lineTo(star.getCurrentPoint().getX() + 6, star.getCurrentPoint().getY());
            star.moveTo(center.getX(), center.getY());
            star.lineTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() + 5);
            star.moveTo(star.getCurrentPoint().getX() - 3, star.getCurrentPoint().getY());
            star.lineTo(star.getCurrentPoint().getX() + 6, star.getCurrentPoint().getY());
            star.moveTo(center.getX(), center.getY());
            star.lineTo(star.getCurrentPoint().getX() - 5, star.getCurrentPoint().getY());
            star.moveTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() - 3);
            star.lineTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() + 6);
            star.moveTo(center.getX(), center.getY());
            star.lineTo(star.getCurrentPoint().getX() + 5, star.getCurrentPoint().getY());
            star.moveTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() - 3);
            star.lineTo(star.getCurrentPoint().getX(), star.getCurrentPoint().getY() + 6);
            if (i == this.selectedMarker) {
                lastMarker = star;
            } else {
                canvas.draw(star);
                canvas.fill(star);
            }
        }

        if (lastMarker != null) {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }
    }

    protected boolean isSpecialPoint(double y) {
        //marker paint
        int intFunc = (int) y;

        while(intFunc > 0){
            if((intFunc%10)%2 != 0)
                return false;
            intFunc /= 10;
        }

        return true;
    }


    private void paintLabels(Graphics2D canvas) {
        // Axis And Grid
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        double labelXPos;
        if (!(this.minY >= 0 || this.maxY <= 0))
            labelYPos = 0;
        else labelYPos = this.minY;
        if (!(this.minX >= 0 || this.maxX <= 0.0D))
            labelXPos = 0;
        else labelXPos = this.minX;
        double pos = this.minX;
        double step = (this.maxX - this.minX) / 10;
        while (pos < this.maxX) {
            java.awt.geom.Point2D.Double point = xyToPoint(pos, labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float) (point.getX() + 5), (float) (point.getY() - bounds.getHeight()));
            pos = pos + step;
        }
        pos = this.minY;
        step = (this.maxY - this.minY) / 10.0D;
        while (pos < this.maxY) {
            Point2D.Double point = xyToPoint(labelXPos, pos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float) (point.getX() + 5), (float) (point.getY() - bounds.getHeight()));
            pos = pos + step;
        }
        if (selectedMarker >= 0) {
            Point2D.Double point = xyToPoint(((Double[]) graphicsData.get(selectedMarker))[0],
                    ((Double[]) graphicsData.get(selectedMarker))[1]);
            String label = "X=" + formatter.format(((Double[]) graphicsData.get(selectedMarker))[0]) +
                    ", Y=" + formatter.format(((Double[]) graphicsData.get(selectedMarker))[1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLACK);
            canvas.drawString(label, (float) (point.getX() + 5.0D), (float) (point.getY() - bounds.getHeight()));
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        scaleX = this.getSize().getWidth() / (this.maxX - this.minX);
        scaleY = this.getSize().getHeight() / (this.maxY - this.minY);
        if ((this.graphicsData == null) || (this.graphicsData.size() == 0)) return;

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();
        Paint oldPaint = canvas.getPaint();

        if (rotateLeft) {
            AffineTransform at = AffineTransform.getRotateInstance(-Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2);
            at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
                    (getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2));
            canvas.setTransform(at);
        }
        if (showGrid)
            paintGrid(canvas);

        if (showAxis) {
            paintAxis(canvas);
            paintLabels(canvas);
        }

        paintGraphics(canvas);

        if (showMarkers)
            paintMarkers(canvas);

        paintSelection(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

    }

    private void paintSelection(Graphics2D canvas) {
        if (!scaleMode) return;
        canvas.setStroke(selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(selectionRect);
    }

    // set left
    public void setRotateLeft(boolean rotateLeft) {
        this.rotateLeft = rotateLeft;
        repaint();
    }

    // Сбрасываем изменения
    public void reset() {
        showGraphics(this.originalData);
    }
}