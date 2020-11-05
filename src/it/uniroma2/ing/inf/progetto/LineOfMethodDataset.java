package it.uniroma2.ing.inf.progetto;

public class LineOfMethodDataset {

	private int version;//id release
	private String method;
	private int methodHistories;
	private int authors;
	private int stmtAdded;
	private int maxStmtAdded;
	private int avgStmtAdded;
	private int stmtDeleted;
	private int maxStmtDeleted;
	private int avgStmtDeleted;
	private int churn;
	private int maxChurn;
	private int avgChurn;
	private int decl;
	private int cond;
	private int elseAdded;
	private int elseDeleted;
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
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	/**
	 * @return the methodHistories
	 */
	public int getMethodHistories() {
		return methodHistories;
	}
	/**
	 * @param methodHistories the methodHistories to set
	 */
	public void setMethodHistories(int methodHistories) {
		this.methodHistories = methodHistories;
	}
	/**
	 * @return the authors
	 */
	public int getAuthors() {
		return authors;
	}
	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(int authors) {
		this.authors = authors;
	}
	/**
	 * @return the stmtAdded
	 */
	public int getStmtAdded() {
		return stmtAdded;
	}
	/**
	 * @param stmtAdded the stmtAdded to set
	 */
	public void setStmtAdded(int stmtAdded) {
		this.stmtAdded = stmtAdded;
	}
	/**
	 * @return the maxStmtAdded
	 */
	public int getMaxStmtAdded() {
		return maxStmtAdded;
	}
	/**
	 * @param maxStmtAdded the maxStmtAdded to set
	 */
	public void setMaxStmtAdded(int maxStmtAdded) {
		this.maxStmtAdded = maxStmtAdded;
	}
	/**
	 * @return the avgStmtAdded
	 */
	public int getAvgStmtAdded() {
		return avgStmtAdded;
	}
	/**
	 * @param avgStmtAdded the avgStmtAdded to set
	 */
	public void setAvgStmtAdded(int avgStmtAdded) {
		this.avgStmtAdded = avgStmtAdded;
	}
	/**
	 * @return the stmtDeleted
	 */
	public int getStmtDeleted() {
		return stmtDeleted;
	}
	/**
	 * @param stmtDeleted the stmtDeleted to set
	 */
	public void setStmtDeleted(int stmtDeleted) {
		this.stmtDeleted = stmtDeleted;
	}
	/**
	 * @return the maxStmtDeleted
	 */
	public int getMaxStmtDeleted() {
		return maxStmtDeleted;
	}
	/**
	 * @param maxStmtDeleted the maxStmtDeleted to set
	 */
	public void setMaxStmtDeleted(int maxStmtDeleted) {
		this.maxStmtDeleted = maxStmtDeleted;
	}
	/**
	 * @return the avgStmtDeleted
	 */
	public int getAvgStmtDeleted() {
		return avgStmtDeleted;
	}
	/**
	 * @param avgStmtDeleted the avgStmtDeleted to set
	 */
	public void setAvgStmtDeleted(int avgStmtDeleted) {
		this.avgStmtDeleted = avgStmtDeleted;
	}
	/**
	 * @return the churn
	 */
	public int getChurn() {
		return churn;
	}
	/**
	 * @param churn the churn to set
	 */
	public void setChurn(int churn) {
		this.churn = churn;
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
	 * @return the avgChurn
	 */
	public int getAvgChurn() {
		return avgChurn;
	}
	/**
	 * @param avgChurn the avgChurn to set
	 */
	public void setAvgChurn(int avgChurn) {
		this.avgChurn = avgChurn;
	}
	/**
	 * @return the decl
	 */
	public int getDecl() {
		return decl;
	}
	/**
	 * @param decl the decl to set
	 */
	public void setDecl(int decl) {
		this.decl = decl;
	}
	/**
	 * @return the cond
	 */
	public int getCond() {
		return cond;
	}
	/**
	 * @param cond the cond to set
	 */
	public void setCond(int cond) {
		this.cond = cond;
	}
	/**
	 * @return the elseAdded
	 */
	public int getElseAdded() {
		return elseAdded;
	}
	/**
	 * @param elseAdded the elseAdded to set
	 */
	public void setElseAdded(int elseAdded) {
		this.elseAdded = elseAdded;
	}
	/**
	 * @return the elseDeleted
	 */
	public int getElseDeleted() {
		return elseDeleted;
	}
	/**
	 * @param elseDeleted the elseDeleted to set
	 */
	public void setElseDeleted(int elseDeleted) {
		this.elseDeleted = elseDeleted;
	}
	
	
	
	public LineOfMethodDataset(int version, String method) {
		super();
		this.version = version;
		this.method = method;
		this.authors=0;
		this.avgChurn=0;
		this.avgStmtAdded=0;
		this.avgStmtDeleted=0;
		this.churn=0;
		this.cond=0;
		this.decl=0;
		this.elseAdded=0;
		this.elseDeleted=0;
		this.maxChurn=0;
		this.maxStmtAdded=0;
		this.maxStmtDeleted=0;
		this.methodHistories=0;
		this.stmtAdded=0;
		this.avgStmtDeleted=0;
		
	}
	
	
	
	
}
