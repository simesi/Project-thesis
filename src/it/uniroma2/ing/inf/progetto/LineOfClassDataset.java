package it.uniroma2.ing.inf.progetto;

public class LineOfClassDataset {

	
	private int version;//id versione
	private String fileName;
	private int size;
	private int maxChurn;
	private int avgChurn;
	private int locTouched;
	private int nr;
	private int churn;
	private int nAuth;
	private int locAdded;
	private int maxLOCAdded;
	private int avgLOCAdded;
	private String buggy= "NO";
	private int nFix=0;	
	private int age;
	private double WeightedAge;
	private int chgSetSize;
	private int maxChgSet;
	private int avgChgSet;
	
	
	
	
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}



	/**
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}



	/**
	 * @return the weightedAge
	 */
	public double getWeightedAge() {
		return WeightedAge;
	}



	/**
	 * @param weightedAge the weightedAge to set
	 */
	public void setWeightedAge(double weightedAge) {
		WeightedAge = weightedAge;
	}



	/**
	 * @return the chgSetSize
	 */
	public int getChgSetSize() {
		return chgSetSize;
	}



	/**
	 * @param chgSetSize the chgSetSize to set
	 */
	public void setChgSetSize(int chgSetSize) {
		this.chgSetSize = chgSetSize;
	}



	/**
	 * @return the maxChgSet
	 */
	public int getMaxChgSet() {
		return maxChgSet;
	}



	/**
	 * @param maxChgSet the maxChgSet to set
	 */
	public void setMaxChgSet(int maxChgSet) {
		this.maxChgSet = maxChgSet;
	}



	/**
	 * @return the avgChgSet
	 */
	public int getAvgChgSet() {
		return avgChgSet;
	}



	/**
	 * @param avgChgSet the avgChgSet to set
	 */
	public void setAvgChgSet(int avgChgSet) {
		this.avgChgSet = avgChgSet;
	}



	/**
	 * @param nFix the nFix to set
	 */
	public void setnFix(int nFix) {
		this.nFix = nFix;
	}


	
	/**
	 * @return the nFix
	 */
	public int getnFix() {
		return nFix;
	}





	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}


	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}


	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the maxChurn
	 */
	public int getMaxChurn() {
		return maxChurn;
	}


	/**
	 * @param maxChurn the maxChurn to set
	 */
	public void setMaxChurn(int maxChurn) {
		this.maxChurn = maxChurn;
	}


	/**
	 * @return the aVG_Churn
	 */
	public int getAVGChurn() {
		return avgChurn;
	}


	/**
	 * @param aVG_Churn the aVG_Churn to set
	 */
	public void setAVGChurn(int avgChurn) {
		this.avgChurn = avgChurn;
	}
	
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}


	/**
	 * @param size the size to set
	 */
	public void setSize(int mySize) {
		size = mySize;
	}


	/**
	 * @return the lOC_Touched
	 */
	public int getLOCTouched() {
		return locTouched;
	}


	/**
	 * @param lOC_Touched the lOC_Touched to set
	 */
	public void setLOCTouched(int locTouched) {
		this.locTouched = locTouched;
	}


	/**
	 * @return the nR
	 */
	public int getNR() {
		return nr;
	}


	/**
	 * @param nR the nR to set
	 */
	public void setNR(int nR) {
		nr = nR;
	}


	/**
	 * @return the Churn
	 */
	public int getChurn() {
		return churn;
	}


	/**
	 * @param Churn the Churn to set
	 */
	public void setChurn(int churn) {
		this.churn = churn;
	}


	/**
	 * @return the nAuth
	 */
	public int getNauth() {
		return nAuth;
	}


	/**
	 * @param nAuth the nAuth to set
	 */
	public void setNauth(int nAuth) {
		this.nAuth = nAuth;
	}


	/**
	 * @return the lOC_Added
	 */
	public int getLocadded() {
		return locAdded;
	}


	/**
	 * @param lOC_Added the lOC_Added to set
	 */
	public void setLOCAdded(int locAdded) {
		this.locAdded = locAdded;
	}


	/**
	 * @return the mAX_LOC_Added
	 */
	public int getMAXLOCAdded() {
		return maxLOCAdded;
	}


	/**
	 * @param mAX_LOC_Added the mAX_LOC_Added to set
	 */
	public void setMAXLOCAdded(int maxLOCAdded) {
		this.maxLOCAdded = maxLOCAdded;
	}


	/**
	 * @return the aVG_LOC_Added
	 */
	public int getAVGLOCAdded() {
		return avgLOCAdded;
	}


	/**
	 * @param aVG_LOC_Added the aVG_LOC_Added to set
	 */
	public void setAVGLOCAdded(int avgLOCAdded) {
		this.avgLOCAdded = avgLOCAdded;
	}

	
	/**
	 * @return the buggy
	 */
	public String getBuggy() {
		return buggy;
	}


	/**
	 * @param buggy the buggy to set
	 */
	public void setBuggy(String buggy) {
		this.buggy = buggy;
	}


	

             //costruttore minimale                                             
	public LineOfClassDataset(int version, String fileName) {
		super();
		this.version = version;
		this.fileName = fileName;
		
	}

	

}
