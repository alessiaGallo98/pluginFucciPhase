package com.unipv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;


public class IO {
	private String path;
    private String filePathSave;
    private String filePathSaveXLSX;
    private String filePathSaveXML;
    private String filePathSaveSorted;
    private String filePathSaveXLSXSorted;
    private String filePathAnalysis;
    private String filePathAnalysisXLSX;
    File fileCSV;
    File fileXML;
    
    String labelsText;
    String allText;
    int nSpots;
     
    public IO (String path, File fileCSV, File fileXML) {
    	nSpots = 0;
        allText = "";
    	this.path = path;
    	filePathSave = path + "\\result.csv";
        filePathSaveXML = path + "\\result.xml";
        filePathSaveXLSX = path + "\\result.xlsx";
        filePathSaveSorted = path + "\\result_modified.csv";
        filePathSaveXLSXSorted = path + "\\result_modified.xlsx";
        filePathAnalysis = path + "\\result_Analysis.csv";
        filePathAnalysisXLSX = path + "\\result_Analysis.xlsx";
        this.fileXML=fileXML;
        this.fileCSV=fileCSV;
    } 
    
    public String getPath() {
    	return path;
    }
    
    public String getText() {
    	return allText;
    }

	
    public Data loadFromFile() throws IOException {
        int count = 0;
        String[] labels = null;
        
        ArrayList<Double> meanIntensityCh3 = new ArrayList<Double>();
        ArrayList<Double> meanIntensityCh4 = new ArrayList<Double>();
        ArrayList<Integer> trackId = new ArrayList<Integer>();
        ArrayList<Integer> frame = new ArrayList<Integer>();
        ArrayList<Double> t = new ArrayList<Double>();
        ArrayList<Double> positionX = new ArrayList<Double>();
        ArrayList<Double> positionY = new ArrayList<Double>();
        nSpots=0;

        FileReader fr = new FileReader(fileCSV);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (count==0) {
                labels = line.split(",");
            }

            if (count>=4) {
                String[] columns = line.split(",");
                for (int i=0; i<(labels.length); i++){
                    if (labels[i].equals("TRACK_ID")) {
                    	if(!columns[i].equals("")) {
                    		trackId.add(count-4, Integer.parseInt(columns[i]));
                    	}
                    	else {
                    		trackId.add(count-4,-1);
                    	}
                    	
                    }
                    if (labels[i].equals("FRAME")){
                    	frame.add(count-4, Integer.parseInt(columns[i]));
                    }
                    if (labels[i].equals("MEAN_INTENSITY_CH3")){
                        meanIntensityCh3.add(count-4, Double.parseDouble(columns[i]));
                    }
                    if (labels[i].equals("MEAN_INTENSITY_CH4")){
                    	meanIntensityCh4.add(count-4, Double.parseDouble(columns[i]));
                    }
                    if (labels[i].equals("POSITION_T")){
                    	t.add(count-4, Math.round((Double.parseDouble(columns[i])/(60*60))*100.0)/100.0);
                    }
                    if (labels[i].equals("POSITION_X")){
                    	positionX.add(count-4, Double.parseDouble(columns[i]));
                    }
                    if (labels[i].equals("POSITION_Y")){
                    	positionY.add(count-4, Double.parseDouble(columns[i]));
                    }
                }
            }
            count = count+1;
        }
        nSpots = count-4;
        
        int[] trackIdVector = new int[nSpots];
    	int[] frameVector = new int[nSpots];
    	double[] meanIntensityCh3Vector = new double[nSpots];
    	double[] meanIntensityCh4Vector = new double[nSpots];
    	double[] tVector = new double[nSpots];
    	double[] positionXVector = new double[nSpots];
    	double[] positionYVector = new double[nSpots];
    	for (int i=0; i<nSpots; i++) {
			trackIdVector[i] = trackId.get(i);
			frameVector[i] = frame.get(i);
			meanIntensityCh3Vector[i] = meanIntensityCh3.get(i);
	    	meanIntensityCh4Vector[i] = meanIntensityCh4.get(i);
	    	tVector[i] = t.get(i);
	    	positionXVector[i] = positionX.get(i);
	    	positionYVector[i] = positionY.get(i);
		}
    	
    	return new Data(meanIntensityCh3Vector, meanIntensityCh4Vector, trackIdVector,
    			frameVector, positionXVector, positionYVector, tVector);
        
    }

	public void save(Data data)throws Exception {
        // file da leggere
        FileReader fr = new FileReader(this.fileCSV);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        int count = 0; // serve a contare a che riga siamo, per escludere le prime
        
        // file da scrivere
        File fileWrite = new File(filePathSave);
        fileWrite.createNewFile();
        FileWriter fw = new FileWriter(fileWrite);
        BufferedWriter bw = new BufferedWriter(fw);
        while ((line = br.readLine()) != null) {
            String[] text = line.split("\n");
            if (count==0) {
            	labelsText = (text[0] + ",SMOOTH_CH3,SMOOTH_CH4,MEAN_INTENSITY_CH3_NORM,"
            			+ "MEAN_INTENSITY_CH4_NORM,T,PHASE,gray_Value" + "\n");
                bw.write(labelsText);
            }
            if (count>=4){
                bw.write(text[0] + "," + data.getSmoothCyan()[count-4] + "," + data.getSmoothMagenta()[count-4] 
                		+ "," + data.getMeanIntensityCh3Norm()[count-4] + "," + data.getMeanIntensityCh4Norm()[count-4] 
                		+ "," + data.getT()[count-4] + "," + data.getPhase()[count-4] + "," 
                		+ data.getGrayValue()[count-4] + "\n");
                allText = allText + text[0] + "," + data.getSmoothCyan()[count-4] + "," + 
                		+ data.getSmoothMagenta()[count-4] + "," + data.getMeanIntensityCh3Norm()[count-4] + "," 
                		+ data.getMeanIntensityCh4Norm()[count-4] + "," + data.getT()[count-4] + "," 
                		+ data.getPhase()[count-4] + "," + data.getGrayValue()[count-4] + "\n";
            }
            count++;
        }
        bw.flush();
        bw.close();
        
        
        // Salvo in formato XML
        SAXBuilder builder = new SAXBuilder();
	    Document document = builder.build(fileXML);
	    Element root = document.getRootElement();
	    List<?> children = (List<?>) root.getChildren();
	    Iterator<?> iterator = children.iterator();
	    while(iterator.hasNext()){
	    	Element item = (Element)iterator.next();
	    	Element allSpots = item.getChild("AllSpots");
	    	if(allSpots!=null) {
	    		List<?> listChildren = (List<?>)allSpots.getChildren();
    	    	Iterator<?> iterator2 = listChildren.iterator();
    	    	int i=0;
    	    	while(iterator2.hasNext()){
    	    		Element item2 = (Element)iterator2.next();
    	    		List<Element> spots = item2.getChildren("Spot");
    	    		for (int j=0; j<spots.size(); j++) {
	    	    		Element spotReference = spots.get(j);
	    	    		if(spotReference!=null) {
		    	    		spotReference.setAttribute("name", data.getName()[i]+"%");
		    	    		spotReference.setAttribute("MANUAL_SPOT_COLOR", data.getNewColumnColor()[i]+"");
		    	    		i++;
	    	    		}
    	    		}
    	    	}
	    	}
	    }
		org.jdom2.output.XMLOutputter outputter = new org.jdom2.output.XMLOutputter();
		outputter.setFormat(org.jdom2.output.Format.getPrettyFormat());
		outputter.output(document, new FileOutputStream(this.filePathSaveXML));
		
        
        // Salvo anche in formato XLSX
        Workbook workbook = new Workbook(filePathSave);
    	workbook.save(filePathSaveXLSX, SaveFormat.XLSX);    
    }

    // Salvataggio su file csv e xlsx dei dati ordinati
	public void saveSortedText(String text, int[] indexOrdered) throws Exception {
		this.orderText(indexOrdered);
		String labels = labelsText.split("\n")[0] + "\n";
		boolean isSorted = true;
		this.save(this.allText, isSorted, labels);
    }

    /* Per ogni cellula presente salva i dati statistici per l'analisi del movimento cellulare 
 	suddivisi in fase G1 e S/G2/M.*/
	public void saveStatisticAnalysis(String text) throws Exception {
		String labels = "TRACK_ID,PHASE,FIRST_FRAME,LAST_FRAME,MEAN_SPEED,TRACK_LENGTH,"
				+ "DISPLACEMENT,STRAIGHTNESS, OVERALL_ANGLE, OVERALL_DOT, OVERALL_NORM_DOT" + "\n";
		boolean isSorted = false;
		this.save(text, isSorted, labels);
	}
	
	public void save(String text, boolean isSorted, String labels) throws Exception {
		String filePath = "";
		if(isSorted == true) {
			filePath = this.filePathSaveSorted;
		}
		else {
			filePath = this.filePathAnalysis;
		}
		File file_write = new File(filePath);
		file_write.createNewFile();
		FileWriter fw = new FileWriter(file_write);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(labels);
		bw.write(text);
		bw.flush();
		bw.close();
		
		Workbook workbook = new Workbook(filePath);
		if(isSorted) {
			workbook.save(this.filePathSaveXLSXSorted, SaveFormat.XLSX);
		}
		else {
			workbook.save(this.filePathAnalysisXLSX, SaveFormat.XLSX);
		}
	}
	
	public void orderText(int[] indexOrdered) {
		// salvo testo ordinato
    	String[] testoArray = this.allText.split("\n");
    	String textOrdered = "";
    	for (int k=0; k<nSpots; k++) {
    		int index = indexOrdered[k];
    		textOrdered = textOrdered + testoArray[index] + "\n";
    	}
    	
    	this.allText = textOrdered;
	}

}
