package it.uniroma2.ing.inf.progetto;

import java.util.List;

public class TicketTakenFromJIRA {
     private String key;
     private String createdVersionIndex;
     private String affectedVersionIndex;
     private String fixedVersion;
     private List<String> filenames;
     
     
     
	public TicketTakenFromJIRA(String key, String createdVersion, String affectedVersion) {
		super();
		this.key = key;
		this.createdVersionIndex = createdVersion;
		affectedVersionIndex = affectedVersion;
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
	 * @return the affectedVersion
	 */
	public String getAffectedVersion() {
		return affectedVersionIndex;
	}



	/**
	 * @param affectedVersion the affectedVersion to set
	 */
	public void setAffectedVersion(String affectedVersion) {
		affectedVersionIndex = affectedVersion;
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
	 * @return the filenames
	 */
	public List<String> getFilenames() {
		return filenames;
	}




	/**
	 * @param filenames the filenames to set
	 */
	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}

}
