/*
 * MSC - Molecule Set Comparator
 * Copyright (C) 2020 Jan-Mathis Hein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.whs.ibci.msc.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the cancelation and execution of the ParseAndCompareTasks. This class
 * also communicates its progress and handles the task's inputs.
 *
 * @author Jan-Mathis Hein
 */
public class JobTaskManager implements PropertyChangeListener{
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * True if this TaskManager is initializing task, executing tasks or 
     * processing the results of tasks, false otherwise. Is also used for 
     * cancelation
     */
    private boolean isStillWorking;
    
    /**
     * True if a valid first input type has been set, false otherwise
     */
    private boolean hasValidInputType1;
    
    /**
     * True if a valid second input type has been set, false otherwise
     */
    private boolean hasValidInputType2;
    
    /**
     * True if a valid first input file has been chosen, false otherwise
     */
    private boolean hasValidInputFile1;
    
    /**
     * True if a valid second input file has been chosen, false otherwise
     */
    private boolean hasValidInputFile2;
    
    /**
     * True if the HistogramDataManager has been created, false otherwise
     */
    private boolean hasCreatedHistogramDataManager;
    
    /**
     * An array that defines which features will be considered for 
     * the comparison. The index corresponds to the number of the feature
     */
    private boolean[] considerFeature;
    
    /**
     * Highest task progress value reached so far
     */
    private double highestProgressValue;
    
    /**
     * Number of created ParseAndCompareTasks
     */
    private double numberOfParseAndCompareTasks;
    
    /**
     * ExecutorService that handles threads and the execution of the tasks
     */
    private ExecutorService executorService;
    
    /**
     * First input file which contains a string encoding of the molecule set
     */
    private File inputFile1;
    
    /**
     * Second input file which contains a string encoding of the molecule set
     */
    private File inputFile2;
    
    /**
     * Manages the histogram data for each ComparisonFeature
     */
    private HistogramDataManager histogramDataManager;
    
    /**
     * Type of input of the first input file
     */
    private InputType inputType1;
    
    /**
     * Type of input of the second input file
     */
    private InputType inputType2;
    
    /**
     * Number of threads that can be used concurrently by the ExecutorService
     */
    private int numberOfParallelThreads;
    
    /**
     * Default number of bins in a histogram
     */
    private int defaultNumberOfBins;
    
    /**
     * Number of inputs that one set has as a surplus
     */
    private int numberOfUnpairedInputs;
    
    /**
     * List of features that are considered for the comparison. Is never null
     */
    private final List<ComparisonFeature> consideredFeaturesList = new LinkedList<>();
    
    /**
     * PropertyChangeSupport that manages the firing of PropertyChangeEvents and 
     * the adding and removing of PropertyChangeListeners
     */
    private final PropertyChangeSupport propertyChangeSupport;
    
    /**
     * Queue of ComparisonResults of ParseAndCompareTasks that finished 
     * successfully. Is never null
     */
    private final Queue<ComparisonResult> comparisonResultsQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of ComparisonResults of ParseAndCompareTask that did not finish 
     * successfully. Is never null
     */
    private final Queue<ComparisonResult> failedComparisonsQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of ParseAndCompareTasks that were initialized but not 
     * submitted for execution yet. Is never null
     */
    private final Queue<ParseAndCompareTask> remainingParseAndCompareTasksQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Queue of ParseAndCompareTasks that have been submitted for 
     * execution. Is never null
     */
    private final Queue<ParseAndCompareTask> submittedParseAndCompareTasksQueue = new ConcurrentLinkedQueue<>();
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables
     * 
     * @param tmpNumberOfParallelThreads number of threads that will be used in 
     * parallel
     * @param tmpDefaultNumberOfBins default number of bins used for histograms
     */
    public JobTaskManager(int tmpNumberOfParallelThreads, int tmpDefaultNumberOfBins) {
        this.hasCreatedHistogramDataManager = false;
        this.isStillWorking = false;
        this.hasValidInputType1 = false;
        this.hasValidInputType2 = false;
        this.hasValidInputFile1 = false;
        this.hasValidInputFile2 = false;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.numberOfParallelThreads = tmpNumberOfParallelThreads;
        this.defaultNumberOfBins = tmpDefaultNumberOfBins;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="- Task related methods">
    /**
     * True if this JobTaskManager is initializing tasks, executing tasks or 
     * processing the results of tasks AND was not canceled, false otherwise
     * 
     * @return true if this JobTaskManager is initializing tasks, executing 
     * tasks or processing the results of tasks AND was not canceled, 
     * false otherwise
     */
    public boolean isWorking() {
        return this.isStillWorking;
    }
    
    /**
     * Cancel the working process of this JobTaskManager i.e. the 
     * initialization, execution or result processing of tasks. The 
     * ExecutorService will be shutdown and disposed of. After cancelation the 
     * JobTaskManager can be reused.
     */
    public void cancelWorkingProcess() {
        this.isStillWorking = false;
        synchronized (this) {
            int tmpNumberOfSubmittedTasks = this.submittedParseAndCompareTasksQueue.size();
            for (int i = 0; i < tmpNumberOfSubmittedTasks; i++) {
                try {
                    ParseAndCompareTask tmpTask = this.submittedParseAndCompareTasksQueue.poll();
                    tmpTask.cancel();
                    tmpTask.removePropertyChangeListener(this);
                } catch (NullPointerException ex) {
                    // This can happen if the task finishes and in the process removes itself from the queue
                }
            }
            this.remainingParseAndCompareTasksQueue.clear();
            this.comparisonResultsQueue.clear();
            this.failedComparisonsQueue.clear();
            if (this.executorService != null) {
                this.executorService.shutdown();
                this.executorService = null;
            }
        }
    }
    
    /**
     * Initialize the tasks and start their execution
     * 
     * @throws IOException if there is something wrong with the input files
     * @throws MSCInputException if the input is not sufficient 
     */
    public synchronized void startTasks() throws IOException, MSCInputException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (!(this.hasValidInputFile1 && this.hasValidInputFile2)) {
            throw new MSCInputException("Can't read these files");
        }
        if (this.consideredFeaturesList.isEmpty()) {
            throw new MSCInputException("There are no features to compare");
        }
        if (!(this.hasValidInputType1 && this.hasValidInputType2)) {
            throw new MSCInputException("The input types are invalid");
        }
        //</editor-fold>
        try (Reader tmpReader1 = new BufferedReader(new FileReader(this.inputFile1));
            Reader tmpReader2 = new BufferedReader(new FileReader(this.inputFile2));) {
            // Clears all queues
            this.cancelWorkingProcess();
            this.executorService = Executors.newFixedThreadPool(this.numberOfParallelThreads);
            this.isStillWorking = true;
            this.hasCreatedHistogramDataManager = false;
            this.highestProgressValue = 0;
            this.numberOfParseAndCompareTasks = 0;
            this.numberOfUnpairedInputs = 0;
            int tmpIdentifier = 0;
            // Byte value of the new line character
            int tmpSeperator = 10;
            boolean tmpReachedEOF1, tmpReachedEOF2;
            String tmpMolecule1, tmpMolecule2;
            Object[] tmpArray1, tmpArray2;
            outerLoop:
            while (true) {
                //<editor-fold defaultstate="collapsed" desc="Read next molecules">
                switch (this.inputType1) {
                    case SMILES:
                        tmpArray1 = this.readCharactersUntilSeperator(tmpReader1, tmpSeperator);
                        tmpMolecule1 = (String) tmpArray1[0];
                        tmpReachedEOF1 = (boolean) tmpArray1[1];
                        break;
                    case SDF:
                        tmpArray1 = this.readCharactersUntil$$$$(tmpReader1);
                        tmpMolecule1 = (String) tmpArray1[0];
                        tmpReachedEOF1 = (boolean) tmpArray1[1];
                        break;
                    default:
                        throw new MSCInputException("The first input type is invalid");
                }
                switch (this.inputType2) {
                    case SMILES:
                        tmpArray2 = this.readCharactersUntilSeperator(tmpReader2, tmpSeperator);
                        tmpMolecule2 = (String) tmpArray2[0];
                        tmpReachedEOF2 = (boolean) tmpArray2[1];
                        break;
                    case SDF:
                        tmpArray2 = this.readCharactersUntil$$$$(tmpReader2);
                        tmpMolecule2 = (String) tmpArray2[0];
                        tmpReachedEOF2 = (boolean) tmpArray2[1];
                        break;
                    default:
                        throw new MSCInputException("The second input type is invalid");
                }
                //</editor-fold>
                if (!tmpMolecule1.isEmpty() && !tmpMolecule2.isEmpty()) {
                    ParseAndCompareTask tmpTask = new ParseAndCompareTask(tmpMolecule1, tmpMolecule2, this.inputType1, this.inputType2, this.considerFeature, tmpIdentifier++);
                    tmpTask.addPropertyChangeListener(this);
                    this.remainingParseAndCompareTasksQueue.add(tmpTask);
                    this.numberOfParseAndCompareTasks++;
                } else if (tmpMolecule1.isEmpty() ^ tmpMolecule2.isEmpty()) {
                    this.numberOfUnpairedInputs++;
                }
                if (!this.isStillWorking) {
                    return;
                }
                if (tmpReachedEOF1 && tmpReachedEOF2) {
                    break;
                }
            }
            this.propertyChangeSupport.firePropertyChange("Unpaired inputs", 0, this.numberOfUnpairedInputs);
            if (this.isStillWorking) {
                for (int i = 0; i < this.numberOfParallelThreads; i++) {
                    ParseAndCompareTask tmpNextTask;
                    if ((tmpNextTask = this.remainingParseAndCompareTasksQueue.poll()) != null) {
                        this.submittedParseAndCompareTasksQueue.add(tmpNextTask);
                        this.executorService.submit(tmpNextTask);
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Property change related methods">
    /**
     * Add a PropertyChangeListener that listens for changes fired from this 
     * JobTaskManager
     * 
     * @param tmpListener a listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.addPropertyChangeListener(tmpListener);
    }
    
    /**
     * Remove a PropertyChangeListener
     * 
     * @param tmpListener a listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.removePropertyChangeListener(tmpListener);
    }
    
    /**
     * Handle all PropertyChangeEvents that reach this JobTaskManager.
     * 
     * @param evt PropertyChangeEvent that reached this JobTaskManager
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getSource() instanceof ParseAndCompareTask) {
                ParseAndCompareTask tmpTask = (ParseAndCompareTask) evt.getSource();
                if (!tmpTask.isFinished()) {
                    return;
                }
                switch1:
                switch (evt.getPropertyName()) {
                    case "Task completed":
                        if ((boolean) evt.getNewValue()) {
                            this.comparisonResultsQueue.add(tmpTask.getResult());
                        } else {
                            this.failedComparisonsQueue.add(tmpTask.getResult());
                        }
                        tmpTask.removePropertyChangeListener(this);
                        this.submittedParseAndCompareTasksQueue.remove(tmpTask);
                        double tmpProgress = tmpTask.getIdentifier() / this.numberOfParseAndCompareTasks;
                        if (tmpProgress > this.highestProgressValue && ThreadLocalRandom.current().nextInt(0, 100) < 5) {
                            this.highestProgressValue = tmpProgress;
                            this.propertyChangeSupport.firePropertyChange("ParseAndCompareTasks progress", 0.0, tmpProgress);
                        }
                        synchronized (this) {
                            if (this.isStillWorking && !this.remainingParseAndCompareTasksQueue.isEmpty()) {
                            ParseAndCompareTask tmpNextTask;
                                if ((tmpNextTask = this.remainingParseAndCompareTasksQueue.poll()) != null) {
                                    this.submittedParseAndCompareTasksQueue.add(tmpNextTask);
                                    this.executorService.submit(tmpNextTask);
                                }
                            } else if (this.remainingParseAndCompareTasksQueue.isEmpty() && this.submittedParseAndCompareTasksQueue.isEmpty() && 
                                this.isStillWorking && !this.hasCreatedHistogramDataManager) {
                                Iterator<ComparisonResult> tmpIterator = this.comparisonResultsQueue.iterator();
                                if (!tmpIterator.hasNext()) {
                                    throw new MSCInputException("At least one result is required");
                                }
                                ComparisonResult tmpResult = tmpIterator.next();
                                this.histogramDataManager = new HistogramDataManager(
                                    this.consideredFeaturesList, this.defaultNumberOfBins, tmpResult,
                                    this.inputFile1.getName(), this.inputFile2.getName()
                                );
                                while(tmpIterator.hasNext()) {
                                    if (!this.isStillWorking) {
                                        this.comparisonResultsQueue.clear();
                                        this.failedComparisonsQueue.clear();
                                        this.histogramDataManager = null;
                                        break switch1;
                                    }
                                    tmpResult = tmpIterator.next();
                                    this.histogramDataManager.addDatum(tmpResult);
                                }
                                for (ComparisonFeature tmpComparisonFeature : this.consideredFeaturesList) {
                                    if (!this.isStillWorking) {
                                        this.comparisonResultsQueue.clear();
                                        this.failedComparisonsQueue.clear();
                                        this.histogramDataManager = null;
                                        break switch1;
                                    }
                                    this.histogramDataManager.getHistogramData(tmpComparisonFeature).call();
                                }
                                this.comparisonResultsQueue.clear();
                                this.hasCreatedHistogramDataManager = true;
                                this.propertyChangeSupport.firePropertyChange("ParseAndCompareTasks finished", false, true);
                                this.isStillWorking = false;
                            }
                        }
                        break;
                    default:
                        this.propertyChangeSupport.firePropertyChange("Exception", "JobTaskManager.propertyChange()", new RuntimeException("Unexpected PropertyChange:" + evt.getPropertyName()));
                        break;
                }
            }
        } catch (Exception ex) {
            this.propertyChangeSupport.firePropertyChange("Exception", "JobTaskManager.propertyChange(): " + evt.getPropertyName(), ex);
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Public properties">
    //<editor-fold defaultstate="collapsed" desc="Getters">
    /**
     * Get the HistogramDataManager that manages the histogram data for each
     * ComparisonFeature
     *
     * @return the HistogramDataManager that manages the histogram data for each
     * ComparisonFeature or null if the task execution has not been
     * finished successfully 
     */
    public synchronized HistogramDataManager getHistogramDataManager() {
        return this.hasCreatedHistogramDataManager ? this.histogramDataManager : null;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * Set the ComparisonFeatures which will be considered during the 
     * comparison.
     *
     * @param tmpConsideredFeatures specifies which ComparisonFeatures 
     * will be considered
     * @throws MSCInputException if no ComparisonFeatures are being considered
     */
    public synchronized void setComparisonFeatures(boolean[] tmpConsideredFeatures) throws MSCInputException, IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpConsideredFeatures == null || tmpConsideredFeatures.length < 1) {
            throw new IllegalArgumentException("tmpConsideredFeatures is invalid");
        }
        //</editor-fold>
        this.considerFeature = tmpConsideredFeatures;
        this.consideredFeaturesList.clear();
        for (int i = 0; i < this.considerFeature.length; i++) {
            if (this.considerFeature[i]) {
                this.consideredFeaturesList.add(ComparisonFeature.values()[i]);
            }
        }
        if (this.consideredFeaturesList.size() < 1 || this.consideredFeaturesList.isEmpty()) {
            throw new MSCInputException("There are no features for the comparison");
        }
    }
    
    /**
     * Set the first input file
     *
     * @param tmpFile the input file
     */
    public synchronized void setInputFile1(File tmpFile) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpFile == null) {
            throw new IllegalArgumentException("tmpFile is null");
        }
        //</editor-fold>
        this.inputFile1 = tmpFile;
        this.hasValidInputFile1 = this.inputFile1.canRead();
    }
    
    /**
     * Set the second input file
     *
     * @param tmpFile the input file
     */
    public synchronized void setInputFile2(File tmpFile) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpFile == null) {
            throw new IllegalArgumentException("tmpFile is null");
        }
        //</editor-fold>
        this.inputFile2 = tmpFile;
        this.hasValidInputFile2 = this.inputFile2.canRead();
    }
    
    /**
     * Set the first input type
     *
     * @param tmpInputType the input type
     */
    public synchronized void setInputType1(InputType tmpInputType) {
        this.inputType1 = tmpInputType;
        this.hasValidInputType1 = this.inputType1 == InputType.SMILES || this.inputType1 == InputType.SDF;
    }
    
    /**
     * Set the second input type
     *
     * @param tmpInputType the input type
     */
    public synchronized void setInputType2(InputType tmpInputType) {
        this.inputType2 = tmpInputType;
        this.hasValidInputType2 = this.inputType2 == InputType.SMILES || this.inputType2 == InputType.SDF;
    }
    
    /**
     * Set the number of parallel threads to be used by the executor service and 
     * initialize a new executor service
     * 
     * @param tmpNumberOfParallelThreads specifies the number of parallel 
     * threads
     */
    public synchronized void setNumberOfParallelThreads(int tmpNumberOfParallelThreads) {
        this.numberOfParallelThreads = tmpNumberOfParallelThreads;
        if (this.executorService != null) {
            this.executorService.shutdown();
            this.executorService = Executors.newFixedThreadPool(this.numberOfParallelThreads);
        }
    }
    
    /**
     * Set the default number of bins used for histograms
     * 
     * @param tmpDefaultNumberOfBins specifies the default number of bins used 
     * for histograms
     */
    public void setDefaultNumberOfBins(int tmpDefaultNumberOfBins) {
        this.defaultNumberOfBins = tmpDefaultNumberOfBins;
    }
    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Other methods">
    /**
     * True if input file configuration can be read, false otherwise
     * 
     * @return true if input file configuration can be read, false otherwise
     */
    public boolean canReadInput() {
        return this.hasValidInputType1 && this.hasValidInputType2 && this.hasValidInputFile1 && this.hasValidInputFile2;
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Protected methods">
    /**
     * Finalize this object by stopping the working process, shuting down the 
     * ExecutorService and calling Object.finalize()
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        this.cancelWorkingProcess();
        super.finalize();
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Privat methods">
    /**
     * Read characters from the stream of the given reader until the given 
     * seperator is read. Concatenate all read characters to a String. Return 
     * the String and true if the end of the stream has been reached and false 
     * otherwise
     *
     * @param tmpReader the reader from whos stream the characters will be read
     * @param tmpSeperator specifies the seperator as its byte value
     * @return an Object array where the first element is the concatenated 
     * String and the second element is a Boolean that is true if the end of 
     * the stream has been reached and false otherwise
     * @throws IOException
     */
    private Object[] readCharactersUntilSeperator(Reader tmpReader, int tmpSeperator) throws IOException {
        String tmpMolecule = "";
        Boolean tmpReachedEndOfStream = false;
        int tmpByte;
        while (true) {
            tmpByte = tmpReader.read();
            if (tmpByte == tmpSeperator) {
                break;
            } else if (tmpByte == -1) {
                tmpReachedEndOfStream = true;
                break;
            } else {
                tmpMolecule += (char) tmpByte;
            }
        }
        return new Object[]{tmpMolecule, tmpReachedEndOfStream};
    }
    
    /**
     * Read characters from the stream of the given reader until '$$$$' is read. 
     * Concatenate all read characters to a String. Return the String and true 
     * if the end of the stream has been reached and false otherwise
     *
     * @param tmpReader the reader from whos stream the characters will be read
     * @return an Object array where the first element is the concatenated 
     * String and the second element is a Boolean that is true if the end of 
     * the stream has been reached and false otherwise
     * @throws IOException
     */
    private Object[] readCharactersUntil$$$$(Reader tmpReader) throws IOException {
        String tmpMolecule = "";
        Boolean tmpReachedEndOfStream = false;
        int tmpByte;
        int tmp$Counter = 0;
        loop:
        while (true) {
            tmpByte = tmpReader.read();
            outerSwitch:
            switch (tmpByte) {
                case -1:
                    tmpReachedEndOfStream = true;
                    break loop;
                case 36:
                    tmpMolecule += (char) tmpByte;
                    tmp$Counter++;
                    while (true) {
                        tmpByte = tmpReader.read();
                        switch (tmpByte) {
                            case -1:
                                tmpReachedEndOfStream = true;
                                break loop;
                            case 36:
                                tmpMolecule += (char) tmpByte;
                                tmp$Counter++;
                                break;
                            case 10:
                                tmpMolecule += (char) tmpByte;
                                if (tmp$Counter == 4) {
                                    tmp$Counter = 0;
                                    break loop;
                                }
                                tmp$Counter = 0;
                                break outerSwitch;
                            // Characters that are allowed to follow '$$$$' like \r
                            case 13:
                                tmpMolecule += (char) tmpByte;
                                if (tmp$Counter != 4) {
                                    tmp$Counter = 0;
                                    break outerSwitch;
                                }
                                break;
                            default:
                                tmpMolecule += (char) tmpByte;
                                tmp$Counter = 0;
                                break outerSwitch;
                        }
                    }
                default:
                    tmpMolecule += (char) tmpByte;
                    tmp$Counter = 0;
                    break;
            }
        }
        return new Object[]{tmpMolecule, tmpReachedEndOfStream};
    }
    //</editor-fold>
    
}
