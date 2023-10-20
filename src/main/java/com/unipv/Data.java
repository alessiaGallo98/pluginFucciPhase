package com.unipv;

import java.util.ArrayList;

public class Data {
    private String[] phase;
    private double[] grayValue;
    private int[] newColumnColor;
    private String[] colorUnique;
    private int[] name;
    
    private double[] meanIntensityCh3; 
    private double[] meanIntensityCh4;
    private double[] meanIntensityCh3Norm;
    private double[] meanIntensityCh4Norm;
    double[] smoothCyan;
    double[] smoothMagenta;
    
    private int[] trackId;
    private int[] frame;
    private double[] positionX;
    private double[] positionY;
    private double[] t;
    private int nSpots;
    private int[] indexOrdered;

	public Data(double[] meanIntensityCh3, double[] meanIntensityCh4, int[] trackId,
			int[] frame, double[] positionX, double[] positionY, double[] t) {
		this.meanIntensityCh3 = meanIntensityCh3;
		this.meanIntensityCh4 = meanIntensityCh4;
		this.trackId = trackId;
		this.frame = frame;
		this.positionX = positionX;
		this.positionY = positionY;
		this.t = t;
		this.nSpots= this.positionX.length;
		
		phase = new String[nSpots];
		meanIntensityCh3Norm = new double[nSpots];
		meanIntensityCh4Norm = new double[nSpots];
		smoothCyan = new double[nSpots];
		smoothMagenta = new double[nSpots];
		newColumnColor = new int[nSpots];
	    name = new int[nSpots];
	    grayValue = new double[nSpots];
	    colorUnique = new String[nSpots];
	    
	    indexOrdered = new int[nSpots];
	    
	    createNormValues();
	    createPhase();
	}
	
	private void createNormValues() {
    	smoothCyan = this.movingAverage(meanIntensityCh3);
        smoothMagenta = this.movingAverage(meanIntensityCh4);
        
    	// Calcolo max, min e MEAN_INTENSITY_NORM per ogni canale.
        double maxCh3 = this.max(smoothCyan);
        double maxCh4 = this.max(smoothMagenta);
        double minCh3 = smoothCyan[this.maxIndex(smoothMagenta)];
        double minCh4 = smoothMagenta[this.maxIndex(smoothCyan)];
        meanIntensityCh3Norm = this.norm(smoothCyan,minCh3, maxCh3);
        meanIntensityCh4Norm = this.norm(smoothMagenta, minCh4, maxCh4);
	}
	
	public void createPhase() {
    	/* ###### su 255 valori
    	#EG1 4% da 0 a 3 ---> salto percentuale di 0.04
    	#G1 36% 4 a 40
    	#S 5% 41 a 46
    	#G2M 55% da 47 a 100
    	############
    	#55.3% G2/M --> 12 h
    	#6.8% S --> 1.5
    	#37.9% G1 --> 8.5 h */
	
    	/*
    	 * int[] early_G1_int = IntStream.range(0, 5).toArray();
    	int[] G1_int = IntStream.range(5, 96).toArray();
    	int[] S_int = IntStream.range(96, 114).toArray();
    	int[] S_G2_M_int = IntStream.range(114, 256).toArray();
    	*/
		
    	double[] angleValue = new double[nSpots];
    	
    	ArrayList<Double> sin = new ArrayList<Double>();
    	ArrayList<Double> cos = new ArrayList<Double>();
    	for (int i=0; i<nSpots; i++) {
    		sin.add(i,meanIntensityCh4Norm[i]);
	    	cos.add(i,meanIntensityCh3Norm[i]);
    	}

    	for (int i=0; i<sin.size(); i++) {
    		angleValue[i] = Math.atan2(sin.get(i), cos.get(i));	
    	}
    	
    	double[] meanValuesNorm = this.norm(angleValue,this.min(angleValue), this.max(angleValue));
    	for (int i=0; i<meanValuesNorm.length; i++) {
    		grayValue[i] = meanValuesNorm[i];
    	}
    	
    	double maxEG1 = 0.04;
    	double maxG1 = 0.4;
    	double maxT = 0.46;
    	double maxG2_M = 1;
    	
    	for (int i=0; i<nSpots; i++) {
    		if (grayValue[i]<=maxEG1) {
    			phase[i]="EG1";
    		}
    		if (grayValue[i]<=maxG1 && grayValue[i]>maxEG1) {
    			phase[i]="G1";
    		}
    		if (grayValue[i]<=maxT && grayValue[i]>maxG1) {
    			phase[i]="T";
    		}
    		if (grayValue[i]<=maxG2_M && grayValue[i]>maxT) {
    			phase[i]="G2_M";
    		}
    	}
    	
    	for (int i=0; i<nSpots; i++) {
    		colorUnique[i]= "r=" + (255*grayValue[i]) + ";g=" + 255*grayValue[i] + ";b=" + 255*grayValue[i];
    		newColumnColor[i]=(int)((256*256*grayValue[i] + 256*grayValue[i] +grayValue[i]-(256*256*256)));
    		name[i]= (int) ((grayValue[i]*100));
    	}
	    
    }
	
	
	public int[] getNewColumnColor() {
		return newColumnColor;
	}

	public int[] getName() {
		return name;
	}

	public double[] getSmoothCyan() {
		return smoothCyan;
	}

	public double[] getSmoothMagenta() {
		return smoothMagenta;
	}

	public String[] getPhase() {
		return phase;
	}

	public double[] getGrayValue() {
		return grayValue;
	}

	public int[] getFrame() {
		return frame;
	}

	public double[] getMeanIntensityCh3Norm() {
		return meanIntensityCh3Norm;
	}

	public double[] getMeanIntensityCh4Norm() {
		return meanIntensityCh4Norm;
	}

	public int[] getTrackId() {
		return trackId;
	}

	public double[] getPositionX() {
		return positionX;
	}

	public double[] getPositionY() {
		return positionY;
	}

	public double[] getT() {
		return t;
	}

	public double[] movingAverage(double[] valuesInitial) {
    	// Media mobile
    	double[] meanValues = new double[trackId.length];
    	int length=valuesInitial.length;
    	for(int i=0; i<length; i++) {
    		int trackId = this.trackId[i];
    		ArrayList<Double> values = new ArrayList<Double>();
    		double value = valuesInitial[i];
    		values.add(0,value);
    		int count=1; 
    		int pre = 0;
    		int post = 0;
    		int size = 1;
    		int intervalAverage = 7;
    		while((pre<(intervalAverage) && post<intervalAverage) && ((i-count)>=0 || (i+count)<length)) {
    			if((i+count)<length && post<intervalAverage) {
    				if(this.trackId[i+count]==trackId) {
    					value = valuesInitial[i+count];
    					values.add(size,value);
    					post++;
    					size++;
    				}
    			}
    			if((i-count)>=0 && pre<intervalAverage) {
    				if(this.trackId[i-count]==trackId) {
    					value = valuesInitial[i-count];
    					values.add(size,value);
    					pre++;
    					size++;
    				}
    			}
    			count++;
    		}
    		value = this.mean(values);
    		meanValues[i]=value;
    	}
    	return meanValues;
    }
    
    public double mean(ArrayList<Double> values) {
    	double sum = 0;
    	double count = 0;
    	for (int i=0; i<values.size(); i++) {
    		sum = sum+values.get(i);
    		count++;
    	}
    	return sum/count;
    }
    
    public double mean(double[] values) {
    	double sum = 0;
    	double count = 0;
    	for (int i=0; i<values.length; i++) {
    		sum = sum+values[i];
    		count++;
    	}
    	return sum/count;
    }
	
    public double max(double[] values){
        double max = values[0];
        for (int i=1; i<values.length; i++){
            if (max<values[i])
                max = values[i];
        }
        return max;
    }
    
    public double min(double[] values){
        double min = values[0];
        for (int i=1; i<values.length; i++){
            if (min>values[i]) {
                min = values[i];
            }
        }
        return min;
    }
    
    public int maxIndex(double[] values){
    	double max = values[0];
    	int index = 0;
        for (int i=1; i<values.length; i++){
            if (max<values[i]) {
                max = values[i];
                index = i;
            }
        }
		return index;
    }
    
    public double[] norm(double[] values, double min, double max) {
        double[] valueNorm = new double[values.length];
        for (int i=0; i<values.length; i++){
            valueNorm[i] = (values[i]-min)/(max-min);
        }
        return (valueNorm);
    }
    
	// Ordina per Trackid per Frame - utile per fare poi analisi statistica 
    public void orderTrackidFrame() {
    	ArrayList<Integer> newTrackId = new ArrayList<Integer>();
    	ArrayList<Integer> newFrame = new ArrayList<Integer>();
    	
    	ArrayList<Integer> trackId = new ArrayList<Integer>();
    	ArrayList<Integer> frame = new ArrayList<Integer>();
    	for (int i=0; i<nSpots;i++) {
    		trackId.add(i,this.trackId[i]);
    		frame.add(i,this.frame[i]);
    	}
    	
    	/* Devo prima di tutto salvare i FRAME e i TRACKID in ordine per confrontarli poi
    	 * con l'ordinamento iniziale
    	*/
    	int i=0;
    	// Gestione valori quando il TRACK_ID è assente
    	if(trackId.contains(-1)) {
    		for (int k=0; k<nSpots; k++) {
    			if(trackId.get(k)==-1) {
    				newTrackId.add(i, trackId.get(k));
    				newFrame.add(i, frame.get(k));
    				i++;
    			}
    		}
    		for (int k=(nSpots-1); k>0; k--) {
    			if(trackId.get(k)==-1) {
    				trackId.remove(k);
    	    		frame.remove(k);
    			}
    		}
    	}
    	// ordina TRACK_ID in ordine crescente
    	while(newTrackId.size()!=(nSpots-1)) {
    		int position = this.minIndex(trackId);
    		newTrackId.add(i, trackId.get(position));
    		newFrame.add(i,frame.get(position));
    		trackId.remove(position);
    		frame.remove(position);
    		i++;
    	}
    	newTrackId.add(i, trackId.get(0));
    	newFrame.add(i, frame.get(0));
    	trackId.remove(0);
    	frame.remove(0);
    	
    	// salvo la lista ordinata nuovamente in TRACK_ID e FRAME
    	trackId.addAll(newTrackId);
    	frame.addAll(newFrame);
    	
    	// ora ordino gli elementi con lo stesso TRACK_ID secondo un ordine crescente di FRAME
    	newFrame.clear();
    	newTrackId.clear();
    	i=0;	
    	while(i<(nSpots)) {
    		ArrayList<Integer> framesConsidered = new ArrayList<Integer>();
    		ArrayList<Integer> trackIdConsidered = new ArrayList<Integer>();
    		int valueConsidered = trackId.get(i);
    		int sizeFramesConsidered = 0;
    		for (int j=(i); j<nSpots; j++) {
    			if(trackId.get(j).equals(valueConsidered)) {
    				framesConsidered.add(sizeFramesConsidered, frame.get(j));
    				trackIdConsidered.add(sizeFramesConsidered, trackId.get(j));
    				sizeFramesConsidered++;
    			}
    		}
    		
    		// ordina FRAME con stesso TRACK_ID in ordine crescente
    		while(sizeFramesConsidered!=1) {
    			int position = this.minIndex(framesConsidered);
        		newFrame.add(i, framesConsidered.get(position));
        		newTrackId.add(i, trackIdConsidered.get(position));
        		framesConsidered.remove(position);
        		trackIdConsidered.remove(position);
        		sizeFramesConsidered--;
        		i++;
    		}
    		newFrame.add(i, framesConsidered.get(0));
    		newTrackId.add(i, trackIdConsidered.get(0));
    		framesConsidered.clear();
        	trackIdConsidered.clear();
        	i++;
        	
    	}   	
    	frame.clear();
    	trackId.clear();
    	// salvo la lista ordinata nuovamente in FRAME
    	trackId.addAll(newTrackId);
    	frame.addAll(newFrame);
    	
    	// confronto con le liste precedenti per recuperare gli indici del file originali
    	for (int k=0; k<(nSpots); k++) {
    		for (int j=0; j<(frame.size()); j++) {
	    		if (trackId.get(k).equals(this.trackId[j]) && frame.get(k).equals(this.frame[j])) {
	    			indexOrdered[k]=j;
	    		}
    		}
    	}
    	
    	this.orderColumns(indexOrdered);
    	
    }
    
    // Funzione che calcola l'indice del minimo
    private int minIndex(ArrayList<Integer> value){
        int min = value.get(0);
        int position = 0;
        for (int i=1; i<value.size(); i++){
            if (min>value.get(i)) {
                min = value.get(i);
                position = i;
            }
        }
        return position;
    }
    
    private void orderColumns(int[] indexOrdered) {
        phase = this.order(phase, indexOrdered);
        grayValue = this.order(grayValue, indexOrdered);
        newColumnColor = this.order(newColumnColor, indexOrdered);
        colorUnique = this.order(colorUnique, indexOrdered);
        name = this.order(name, indexOrdered);
        meanIntensityCh3 = this.order(meanIntensityCh3, indexOrdered);
        meanIntensityCh4 = this.order(meanIntensityCh4, indexOrdered);
        meanIntensityCh3Norm = this.order(meanIntensityCh3Norm, indexOrdered);
        meanIntensityCh4Norm = this.order(meanIntensityCh4Norm, indexOrdered);
        smoothCyan = this.order(smoothCyan, indexOrdered);
        smoothMagenta = this.order(smoothMagenta, indexOrdered);
        trackId = this.order(trackId, indexOrdered);
        frame = this.order(frame, indexOrdered);
        positionX = this.order(positionX, indexOrdered);
        positionY = this.order(positionY, indexOrdered);
        t = this.order(t, indexOrdered);
    }
    
    private double[] order(double[] values, int[] indexesSorted) {
    	double[] newLista = new double[nSpots];
    	for (int i=0; i<nSpots; i++) {
    		int index = indexesSorted[i];
    		newLista[i]=values[index];
    	}
    	values=newLista;
    	return values;
    }
    
    private String[] order(String[] values, int[] indexesSorted) {
    	String[] newLista = new String[nSpots];
    	for (int i=0; i<nSpots; i++) {
    		int index = indexesSorted[i];
    		newLista[i]=values[index];
    	}
    	values=newLista;
    	return values;
    }
    
    private int[] order(int[] values, int[] indexesSorted) {
    	int[] newLista = new int[nSpots];
    	for (int i=0; i<nSpots; i++) {
    		int index = indexesSorted[i];
    		newLista[i]=values[index];
    	}
    	values=newLista;
    	return values;
    }
    
    public int[] getIndexOrdered() {
    	return indexOrdered;
    }
	
	
}
