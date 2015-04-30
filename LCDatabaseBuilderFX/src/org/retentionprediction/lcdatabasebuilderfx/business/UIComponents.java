package org.retentionprediction.lcdatabasebuilderfx.business;

import java.util.ArrayList;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class UIComponents {

    /**
     * Draws line from one label to another (This is a part of progress feature available on top of the main screen).
     * @param startBinderLabel
     * @param endBinderLabel
     * @return
     */
    public static Line drawLine(Label startBinderLabel, Label endBinderLabel){
    	Line line = new Line();
    	line.startXProperty().bind(startBinderLabel.layoutXProperty().add(startBinderLabel.widthProperty().add(12.0)));
		line.startYProperty().bind(startBinderLabel.layoutYProperty().add(startBinderLabel.heightProperty().divide(2.0)));
		line.endXProperty().bind(endBinderLabel.layoutXProperty().subtract(12.0));
		line.endYProperty().bind(endBinderLabel.layoutYProperty().add(endBinderLabel.heightProperty().divide(2.0)));
		return line;
    }
    
    /**
     * Draws an arrow at the end of a line.
     * @param line
     * @return
     */
    public static Polygon drawPolygon(Line line){
    	Polygon polygon = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
		polygon.layoutXProperty().bind(line.endXProperty());
		polygon.layoutYProperty().bind(line.endYProperty());
		return polygon;
    }
    
    public static Line drawLineForFinalFit(Label startLabel, Label finalFitLabel){
    	Line line = new Line();
		line.startXProperty().bind(startLabel.layoutXProperty().add(startLabel.widthProperty().add(12.0)));
		line.startYProperty().bind(startLabel.layoutYProperty().add(startLabel.heightProperty().divide(2.0)));
		line.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line.startXProperty()).multiply(2.0/3.0).add(line.startXProperty()));
		line.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));
		return line;
    }
    
    public static Line drawFinalLine(Line line4A, Label finalFitLabel){
    	Line finalLine = new Line();
		finalLine.startXProperty().bind(line4A.endXProperty());
		finalLine.startYProperty().bind(line4A.endYProperty());
		finalLine.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(12.0));
		finalLine.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));
		return finalLine;
    }
    
    public static void setupAllShapes(Pane drawPane, ArrayList<Line> lines, ArrayList<Polygon> polygons, Label finalFitLabel){
    	for(Line l : lines){
    		drawPane.getChildren().add(l);
    	}
    	for(Polygon p : polygons){
    		drawPane.getChildren().add(p);
    	}
    	
    	Line finalLine = UIComponents.drawFinalLine(lines.get(24), finalFitLabel); //24th index is line4A
		Polygon finalPolygon = UIComponents.drawPolygon(finalLine);

		drawPane.getChildren().add(finalLine);
		drawPane.getChildren().add(finalPolygon);
    }
    
    
}
