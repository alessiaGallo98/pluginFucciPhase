package com.unipv;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GUIPlot {
    private double[] meanIntensityCh3Norm;
    private double[] meanIntensityCh4Norm;
    private double[] grayValue;
    
    private int[] trackId;
    private double[] t;
    
    private Plot plot;
    private Plot plot2;
    
    private String path;
    
	public GUIPlot(Data data, String path) {
		this.meanIntensityCh3Norm = data.getMeanIntensityCh3Norm();
		this.meanIntensityCh4Norm = data.getMeanIntensityCh4Norm();
		this.grayValue = data.getGrayValue();
		this.trackId = data.getTrackId();
		this.t = data.getT();
		this.path = path;
	}

	public void plotValue() throws Exception {
	    	
		    plot = Plot.plot(null).series(null, Plot.data().xy(t, this.grayValue), null);
		    BufferedImage bi = plot.save(path + "\\plotUniqueValue", "png");
		    
		    int nSpots = this.meanIntensityCh3Norm.length;
		    double[] meanIntensityCh3Plot = new double[nSpots];
		    double[] meanIntensityCh4Plot = new double[nSpots];
		    for (int i=0; i<this.meanIntensityCh3Norm.length; i++) {
		    	meanIntensityCh3Plot[i] = meanIntensityCh3Norm[i]*255;
		    	meanIntensityCh4Plot[i] = meanIntensityCh4Norm[i]*255;
		    }
		    
		    plot2 = Plot.plot(Plot.plotOpts().legend(Plot.LegendFormat.BOTTOM))
		    	.series("G1 phase CH", Plot.data().xy(t, meanIntensityCh3Plot),Plot.seriesOpts().color(Color.CYAN));
		    plot2.series("S/G2/M phase CH", Plot.data().xy(t, meanIntensityCh4Plot),Plot.seriesOpts().color(Color.MAGENTA));
		    BufferedImage bi2 = plot2.save(path + "\\plotMeanValue", "png");
	    
		    openImages(bi,bi2);
		    
		    // idea, salvare il plotUniqueValue per ciascuno spots. 
		    ArrayList<Integer> trackIdArray = new ArrayList<Integer>();
		    trackIdArray.add(0,this.trackId[0]);
		    for (int i=1; i<this.trackId.length;i++) {
		    	if(!trackIdArray.contains(this.trackId[i])) {
		    		trackIdArray.add(this.trackId[i]);
		    	}
		    }
		    
		    for (int j=0; j<trackIdArray.size();j++) {
			    ArrayList<Double> grayValues = new ArrayList<Double>();
			    ArrayList<Double> magentaValues = new ArrayList<Double>();
			    ArrayList<Double> cyanValues = new ArrayList<Double>();
			    ArrayList<Double> tValues = new ArrayList<Double>();
			    int count = 0;
			    for (int i=0; i<this.trackId.length; i++) {
			    	if(trackIdArray.get(j).equals(this.trackId[i])) {
			    		grayValues.add(count,grayValue[i]);
			    		magentaValues.add(count,meanIntensityCh4Norm[i]);
			    		cyanValues.add(count,meanIntensityCh3Norm[i]);
			    		tValues.add(count,this.t[i]);
			    		count++;
			    	}
			    }
			    plot = Plot.plot(Plot.plotOpts().legend(Plot.LegendFormat.BOTTOM))
			    		.xAxis("h", Plot.axisOpts().range(this.min(tValues), this.max(tValues)))
			    		.yAxis("Value", Plot.axisOpts().range(0, 1))
			    		.series("Gray Value", Plot.data().xy(tValues, grayValues), Plot.seriesOpts().color(Color.BLACK));
			    plot.series("G1 phase CH", Plot.data().xy(tValues, cyanValues),Plot.seriesOpts().color(Color.CYAN));
			    plot.series("S/G2/M phase CH", Plot.data().xy(tValues, magentaValues),Plot.seriesOpts().color(Color.MAGENTA));
			    plot.save(path + "\\plotUniqueValue_TRACKID_" + trackIdArray.get(j), "png");
			    
			    plot = Plot.plot(Plot.plotOpts().legend(Plot.LegendFormat.BOTTOM))
			    		.xAxis("h", Plot.axisOpts().range(this.min(tValues), this.max(tValues)))
			    		.yAxis("Value", Plot.axisOpts().range(0, 1))
			    		.series("Gray Value", Plot.data().xy(tValues, grayValues), Plot.seriesOpts().color(Color.BLACK));
			    plot.save(path + "\\plotGrayValue_TRACKID_" + trackIdArray.get(j), "png");
			    
			    plot = Plot.plot(Plot.plotOpts().legend(Plot.LegendFormat.BOTTOM))
			    		.xAxis("h", Plot.axisOpts().range(this.min(tValues), this.max(tValues)))
			    		.yAxis("Value", null)
			    		.series("G1 phase CH", Plot.data().xy(tValues, cyanValues),Plot.seriesOpts().color(Color.CYAN));
			    plot.series("S/G2/M phase CH", Plot.data().xy(tValues, magentaValues),Plot.seriesOpts().color(Color.MAGENTA));
			    plot.save(path + "\\plotMeanValue_TRACKID_" + trackIdArray.get(j), "png");
			 
		    }
		    
	    }
	
	    public double max(ArrayList<Double> values){
	        double max = values.get(0);
	        for (int i=1; i<values.size(); i++){
	            if (max<values.get(i))
	                max = values.get(i);
	        }
	        return max;
	    }
	    
	    public double min(ArrayList<Double> values){
	        double min = values.get(0);
	        for (int i=1; i<values.size(); i++){
	            if (min>values.get(i)) {
	                min = values.get(i);
	            }
	        }
	        return min;
	    }
	    
	    public void openImages(BufferedImage bi, BufferedImage bi2) throws Exception{
	    	JLabel picture = new JLabel(new ImageIcon(bi));		
	        JFrame frame = new JFrame("Gray Value");
	        frame.add(picture);
	        frame.setSize(1200, 1000);
	        frame.setLocation(50, 50); 
	        frame.setVisible(true);
	        JLabel picture2 = new JLabel(new ImageIcon(bi2));		
	        JFrame frame2 = new JFrame("Mean Value");
	        frame2.add(picture2);
	        frame2.setSize(1200, 1000);
	        frame2.setLocation(900, 50); 
	        frame2.setVisible(true);
	    }
	    
}
