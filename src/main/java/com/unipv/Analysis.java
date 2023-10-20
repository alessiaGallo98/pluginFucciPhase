package com.unipv;

import java.io.IOException;
import java.util.ArrayList;

public class Analysis {
    String text;
    String path;
       
    String[] phase;
    int[] trackId;
    int[] frame;
    double[] positionX;
    double[] positionY;
    double[] t;
    int nCells;
    int nSpots;
    
    public Analysis(String text, Data data, String path) {
		this.text = text;
		nCells = 0;
		this.path = path;
		
		// Ordino prima per TrackId per Frame
		data.orderTrackidFrame();
		
		// Salvo i valori ordinati per iniziare l'analisi
		this.phase = data.getPhase();
		this.positionX = data.getPositionX();
		this.positionY = data.getPositionY();
		this.t = data.getT();
		this.trackId= data.getTrackId();
		this.frame= data.getFrame();
		this.nSpots=frame.length;
		
	}

    // Analisi eseguita per il track_id specificato
    private String cellMovingAnalysis(int trackid, double[] positionX, double[] positionY) throws IOException {
    	/* Ritorna:
    		- FASE G1: Stringa contenente il trackid, la fase, il primo e l'ultimo frame in cui è presente 
    			la cellula e le misure utili all'analisi statistica. 
    	 	- FASE S/G2/M: Stringa contenente il trackid, la fase, il primo e l'ultimo frame in cui è 
    	 		presente la cellula e le misure utili all'analisi statistica. */
    	String textG1 = trackid + "," + "G1";
    	String textSG2 = trackid + "," + "S/G2";
    	
    	/* Serviranno le posizioni specifiche per il calcolo di alcuni
    	 * dati statistici, per questo salvo le sole posizioni per ciascuna
    	 * fase della cellula. Stessa cosa per il Frame.
    	*/
    	ArrayList<Double> xPositionG1 = new ArrayList<Double>();
    	ArrayList<Double> yPositionG1 = new ArrayList<Double>();
    	ArrayList<Double> xPositionSG2 = new ArrayList<Double>();
    	ArrayList<Double> yPositionSG2 = new ArrayList<Double>();
    	ArrayList<Integer> frameG1 = new ArrayList<Integer>();
    	ArrayList<Integer> frameSG2 = new ArrayList<Integer>();
    	for (int i=0; i<nSpots; i++) {
    		if(trackid==this.trackId[i]) {
    			if(this.phase[i].equals("G1") || this.phase[i].equals("EG1")) {
    				xPositionG1.add(positionX[i]);
    				yPositionG1.add(positionY[i]);
    				frameG1.add(this.frame[i]);
    			}
    			if(this.phase[i].equals("G2_M")) { 
    				xPositionSG2.add(positionX[i]);
    				yPositionSG2.add(positionY[i]);
    				frameSG2.add(this.frame[i]);
    			}
    		}
    	}
    	
    	/* Le cellule poco prima della divisione vanno fuori fuoco, 
    	 * elimino gli ultimi quattro dati sulle posizioni e sui frame */
    	for (int i=1; i<=4;i++) {
    		xPositionSG2.remove(xPositionSG2.size()-i);
    		yPositionSG2.remove(yPositionSG2.size()-i);
    		frameSG2.remove(frameSG2.size()-i);
    	}
    	
    	savePlotValueMoving(xPositionG1, yPositionG1, trackid, "G1");
    	savePlotValueMoving(xPositionSG2, yPositionSG2, trackid, "SG2");
    	
    	/* Utilizzo ora i contatori per verificare l'esistenza di FirstFrame e LastFrame
    	 * per ciascuna fase. Se non presenti inserisco "NaN" nel testo, altrimenti segno
    	 * il primo e l'ultimo frame per ciascuna fase.
    	*/
    	if(frameG1.size()>0) {
			textG1 = textG1 + "," + frameG1.get(0) + "," + frameG1.get(frameG1.size()-1);
    	} else {
    		textG1 = textG1 + "," + "NaN" +  "," + "NaN" ;
    	}
    	if(frameSG2.size()>0) {
			textSG2 = textSG2 + "," + frameSG2.get(0) + "," + frameSG2.get(frameSG2.size()-1);
    	} else {
    		textSG2 = textSG2 + "," + "NaN" + "," + "NaN" ;
    	}
    	
    	// Calcolo SPEED e TRACK_LENGTH per ogni dato.
    	double trackLengthG1 = 0.0; double trackLengthSG2 = 0.0;
    	double speedG1 = 0.0;double speedSG2 = 0.0;
    	for (int i=1; i<frame.length; i++) {
    		if (trackId[i]==(trackid) && trackId[i-1]==(trackid)) {
    			double[] diff = {positionX[i-1]-positionX[i], positionY[i-1]-positionY[i]};
    			double value = Math.sqrt(Math.pow(diff[0], 2)+Math.pow(diff[1], 2));
    			if(this.phase[i].equals("G1") || this.phase[i].equals("EG1")) {
    				trackLengthG1 = trackLengthG1 + value;
	    			speedG1 = speedG1 + value/15;
    			}
    			else {
    				trackLengthSG2 = trackLengthSG2 + value;
	    			speedSG2 = speedSG2 + value/15;
    			}
    		}
    	}
    	
    	// Calcolo overallAngle - Angolo tra il primo e l'ultimo segmento del tracking
    	double overallAngleG1 = 0; double overallAngleSG2 = 0;
    	double overallDotG1 = 0; double overallDotSG2 = 0;
    	double overallNormDotG1 = 0; double overallNormDotSG2 = 0;
    	if(frameG1.size()>0) {
	    	overallAngleG1 = this.overallAngle(xPositionG1, yPositionG1);
	    	overallDotG1 = this.overallDot(xPositionG1, yPositionG1);
	    	overallNormDotG1 = this.overallNormDot(xPositionG1, yPositionG1);
    	}
    	if(frameSG2.size()>0) {
	    	overallAngleSG2 = this.overallAngle(xPositionSG2, yPositionSG2);
	    	overallDotSG2 = this.overallDot(xPositionSG2, yPositionSG2);
	    	overallNormDotSG2 = this.overallNormDot(xPositionSG2, yPositionSG2);
    	}
    	
    	// Calcolo dello spostamento (displacement) in ogni fase
    	double displacementG1 = 0; double displacementSG2 = 0;
    	if(frameG1.size()>0) {
	    	double[] diffG1 = {xPositionG1.get(0)-xPositionG1.get(xPositionG1.size()-1), 
	    			yPositionG1.get(0)-yPositionG1.get(yPositionG1.size()-1)};
	    	displacementG1 = Math.sqrt(Math.pow(diffG1[0], 2)+Math.pow(diffG1[1], 2));
    	}
    	if(frameSG2.size()>0) {
	    	double[] diffSG2 = {xPositionSG2.get(0)-xPositionSG2.get(xPositionSG2.size()-1), 
	    			yPositionSG2.get(0)-yPositionSG2.get(yPositionSG2.size()-1)};
	    	displacementSG2 = Math.sqrt(Math.pow(diffSG2[0], 2)+Math.pow(diffSG2[1], 2));
    	}
    	
    	// Calcolo della rettilineità (Straightness)
    	double straightnessG1 = displacementG1/trackLengthG1;
    	double straightnessSG2M = displacementSG2/trackLengthSG2;
    	
    	// Inserisco nel testo i valori statistici trovati per le due fasi.
    	textG1 = textG1 + "," + speedG1/nSpots + "," + trackLengthG1 + "," 
    		+ displacementG1 + "," + straightnessG1 + "," + overallAngleG1 
    		+ "," + overallDotG1 + "," + overallNormDotG1 + "\n";
    	textSG2 = textSG2 + "," + speedSG2/nSpots + "," + trackLengthSG2 
    		+ "," + displacementSG2 + "," + straightnessSG2M + "," + overallAngleSG2 
    		+ "," + overallDotSG2 + "," + overallNormDotSG2 + "\n";
    	return textG1 + textSG2;
    }
    
    // Calcola il prodotto scalare tra il primo e l'ultimo segmento del tracking
    private double overallDot(ArrayList<Double> xPosition, ArrayList<Double> yPosition) {
    	double[] a = {xPosition.get(1)-xPosition.get(0), yPosition.get(1)-yPosition.get(0)};
		double[] b = {xPosition.get(xPosition.size()-1)-xPosition.get(xPosition.size()-2), 
				yPosition.get(yPosition.size()-1)-yPosition.get(yPosition.size()-2)};
		
		return a[0]*b[0]+a[1]*b[1];
    }
    
    private double overallNormDot(ArrayList<Double> xPosition, ArrayList<Double> yPosition) {
    	double[] a = {xPosition.get(1)-xPosition.get(0), yPosition.get(1)-yPosition.get(0)};
		double[] b = {xPosition.get(xPosition.size()-1)-xPosition.get(xPosition.size()-2), 
				yPosition.get(yPosition.size()-1)-yPosition.get(yPosition.size()-2)};
		
		double dot = a[0]*b[0]+a[1]*b[1]; 
		
		double aModule = Math.sqrt(Math.pow(a[0],2)+Math.pow(a[1],2));
		double bModule = Math.sqrt(Math.pow(b[0],2)+Math.pow(b[1],2));
		
		return dot/(aModule*bModule);
    }
    
	private double overallAngle(ArrayList<Double> xPosition, ArrayList<Double> yPosition) {
		double[] a = {xPosition.get(1)-xPosition.get(0), yPosition.get(1)-yPosition.get(0)};
		double[] b = {xPosition.get(xPosition.size()-1)-xPosition.get(xPosition.size()-2), 
				yPosition.get(yPosition.size()-1)-yPosition.get(yPosition.size()-2)};
		
		double prodottoScalare = a[0]*b[0]+a[1]*b[1];
		
		double aModule = Math.sqrt(Math.pow(a[0],2)+Math.pow(a[1],2));
		double bModule = Math.sqrt(Math.pow(b[0],2)+Math.pow(b[1],2));
		
		return Math.acos(prodottoScalare/(aModule*bModule))*180/Math.PI;
	}
	
    // Processo di Analisi Statistica senza media mobile
    public String resultsAnalysis() throws IOException {
    	ArrayList<Integer> cells = new ArrayList<Integer>();
		cells.add(trackId[0]);
		int trackidCurrent = trackId[0];
		for (int i=1; i<trackId.length; i++) {
			if (trackId[i]!=trackidCurrent) {
				cells.add(trackId[i]);
				trackidCurrent = trackId[i];
			}
		}
			
		String newText = "";
		for (int j=0; j<cells.size(); j++) {
			newText = newText + this.cellMovingAnalysis(cells.get(j), this.positionX, this.positionY);
		}
		return newText;
    }
    
	public void savePlotValueMoving(ArrayList<Double> positionX, ArrayList<Double> positionY, int trackId, String phase) throws IOException {
		Plot plot = Plot.plot(null).series(null, Plot.data().xy(positionX, positionY), null);
	    plot.save(path + "\\plotPosition_TRACKID_" + trackId + "_" + phase, "png");
	}
    
 
}
