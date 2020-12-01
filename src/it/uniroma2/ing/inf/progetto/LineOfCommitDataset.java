package it.uniroma2.ing.inf.progetto;

public class LineOfCommitDataset {
	
	private int version;//id release
	private String commit;
	private int numModSub;
	private int numModDir;
	private int numModFiles;
	private double entropy;
	private int lineAdded;
	private int lineDeleted;
	private int lineBeforeChange;
	private String defectFix;
	private int numDev;
	private int age;
	private int nuc;
	private int exp;
	private double recentExp;
	private int subExp;
	private String bugIntroducing= "NO";
	
	
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
	 * @return the commit
	 */
	public String getCommit() {
		return commit;
	}
	/**
	 * @param commit the commit to set
	 */
	public void setCommit(String commit) {
		this.commit = commit;
	}
	/**
	 * @return the numModSub
	 */
	public int getNumModSub() {
		return numModSub;
	}
	/**
	 * @param numModSub the numModSub to set
	 */
	public void setNumModSub(int numModSub) {
		this.numModSub = numModSub;
	}
	/**
	 * @return the numModDir
	 */
	public int getNumModDir() {
		return numModDir;
	}
	/**
	 * @param numModDir the numModDir to set
	 */
	public void setNumModDir(int numModDir) {
		this.numModDir = numModDir;
	}
	/**
	 * @return the entropy
	 */
	public double getEntropy() {
		return entropy;
	}
	/**
	 * @param entropy the entropy to set
	 */
	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
	/**
	 * @return the lineAdded
	 */
	public int getLineAdded() {
		return lineAdded;
	}
	/**
	 * @param lineAdded the lineAdded to set
	 */
	public void setLineAdded(int lineAdded) {
		this.lineAdded = lineAdded;
	}
	/**
	 * @return the lineDeleted
	 */
	public int getLineDeleted() {
		return lineDeleted;
	}
	/**
	 * @param lineDeleted the lineDeleted to set
	 */
	public void setLineDeleted(int lineDeleted) {
		this.lineDeleted = lineDeleted;
	}
	/**
	 * @return the lineBeforeChange
	 */
	public int getLineBeforeChange() {
		return lineBeforeChange;
	}
	/**
	 * @param lineBeforeChange the lineBeforeChange to set
	 */
	public void setLineBeforeChange(int lineBeforeChange) {
		this.lineBeforeChange = lineBeforeChange;
	}
	/**
	 * @return the defectFix
	 */
	public String isDefectFix() {
		return defectFix;
	}
	/**
	 * @param defectFix the defectFix to set
	 */
	public void setDefectFix(String defectFix) {
		this.defectFix = defectFix;
	}
	/**
	 * @return the numDev
	 */
	public int getNumDev() {
		return numDev;
	}
	/**
	 * @param numDev the numDev to set
	 */
	public void setNumDev(int numDev) {
		this.numDev = numDev;
	}
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
	 * @return the nuc
	 */
	public int getNuc() {
		return nuc;
	}
	/**
	 * @param nuc the nuc to set
	 */
	public void setNuc(int nuc) {
		this.nuc = nuc;
	}
	/**
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}
	/**
	 * @param exp the exp to set
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}
	/**
	 * @return the recentExp
	 */
	public double getRecentExp() {
		return recentExp;
	}
	/**
	 * @param recentExp the recentExp to set
	 */
	public void setRecentExp(double recentExp) {
		this.recentExp = recentExp;
	}
	/**
	 * @return the subExp
	 */
	public int getSubExp() {
		return subExp;
	}
	/**
	 * @param subExp the subExp to set
	 */
	public void setSubExp(int subExp) {
		this.subExp = subExp;
	}
	/**
	 * @return the bugIntroducing
	 */
	public String getBugIntroducing() {
		return bugIntroducing;
	}
	/**
	 * @param bugIntroducing the bugIntroducing to set
	 */
	public void setBugIntroducing(String bugIntroducing) {
		this.bugIntroducing = bugIntroducing;
	}
	
	/**
	 * @return the numModFiles
	 */
	public int getNumModFiles() {
		return numModFiles;
	}
	/**
	 * @param numModFiles the numModFiles to set
	 */
	public void setNumModFiles(int numModFiles) {
		this.numModFiles = numModFiles;
	}
	/**
	 * @return the defectFix
	 */
	public String getDefectFix() {
		return defectFix;
	}
	public LineOfCommitDataset(int version, String commit) {
		super();
		this.version = version;
		this.commit = commit;
		this.numModSub=0;
		this.numModDir = 0;
		this.entropy = 0;
		this.lineAdded = 0;
		this.lineDeleted = 0;
		this.lineBeforeChange = 0;
		this.defectFix = "NO";
		this.numDev = 0;
		this.age = 0;
		this.nuc = 0;
		this.exp = 0;
		this.recentExp = 0;
		this.subExp = 0;
		this.bugIntroducing = "NO";	
	}
	
}
