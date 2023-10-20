
package com.unipv;

import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins > FucciPhase")
public class FucciPhase<T extends RealType<T>> implements Command {
	
	@Parameter
	private UIService uiService;
	
	@Parameter
    private OpService opService;
    
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
    	final ImageJ ij = new ImageJ();
        ij.command().run(FucciPhase.class, true);
    }
    
    @Override
    public void run() {
        
    	this.uiService.showDialog("Choose CSV File");
        File fileCSV = this.uiService.chooseFile("Open CSV File",null, "Open");
        this.uiService.showDialog("Choose XML File");
        File fileXML = this.uiService.chooseFile("Open XML File",null, "Open");
        String path = fileCSV.getParent();
        
        IO ioFile = new IO(path, fileCSV, fileXML);
        
        try{
        	/* Estraggo dati dai file e genero anche le nuove colonne del file;
        	 * all'internon della classe Columns vengono anche generati i dati
        	 * della colonna della fase e quelli della colonna della percentuale.
        	*/
        	Data dataColumns = ioFile.loadFromFile();

	        // Salvo i nuovi file
	        ioFile.save(dataColumns);

	        // Creazione e visualizzazione grafici
	        GUIPlot plot = new GUIPlot(dataColumns, ioFile.getPath());
	        plot.plotValue();
	        
	        // ordina e salva
	        Analysis analysis = new Analysis(ioFile.getText(), dataColumns, path);
	        String resultText = analysis.resultsAnalysis();
	        ioFile.saveSortedText(ioFile.getText(),dataColumns.getIndexOrdered()); // prende in input il testo e gli indici per ordinarlo e salvarlo
	        ioFile.saveStatisticAnalysis(resultText); // prende in input i risultati dell'analisi da scrivere
	        
        } catch(Exception e) {
        	System.out.println(e);
        	e.getMessage();
        }
    }
    
}
