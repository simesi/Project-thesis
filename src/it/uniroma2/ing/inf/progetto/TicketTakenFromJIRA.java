package it.uniroma2.ing.inf.progetto;

import java.util.ArrayList;
import java.util.List;

public class TicketTakenFromJIRA {
     private String key;
     private String createdVersionIndex;
     private String fixedVersion;
     private List<String> fixCommitList;
     
     
     
	public TicketTakenFromJIRA(String key, String createdVersion) {
		super();
		this.key = key;
		this.createdVersionIndex = createdVersion;
		this.fixCommitList= new ArrayList<>();
	}




	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}



	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}



	/**
	 * @return the createdVersionIndex
	 */
	public String getCreatedVersion() {
		return createdVersionIndex;
	}



	/**
	 * @param createdVersionIndex the createdVersionIndex to set
	 */
	public void setCreatedVersion(String createdVersion) {
		this.createdVersionIndex = createdVersion;
	}


	/**
	 * @return the fixedVersion
	 */
	public String getFixedVersion() {
		return fixedVersion;
	}



	/**
	 * @param fixedVersion the fixedVersion to set
	 */
	public void setFixedVersion(String fixedVersion) {
		this.fixedVersion = fixedVersion;
	}


	/**
	 * @return the fixCommitList
	 */
	public List<String> getFixCommitList() {
		return fixCommitList;
	}



}
