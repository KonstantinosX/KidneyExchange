package edu.umd.cs.mechdesign.simulator;

public class PatientInformationHolder {
	double CurrTime; 
	WaitlistedPatient patient; 
	DeceasedOrgan organ; 
	String exit_reason;
	
	
	public PatientInformationHolder(double CurrTime, WaitlistedPatient patient, DeceasedOrgan organ, String exit_reason){
		this.CurrTime = CurrTime;
		this.patient = patient;
		this.organ = organ;
		this.exit_reason = exit_reason;
	}
	
	
	
	public double getCurrTime() {
		return CurrTime;
	}
	public void setCurrTime(double currTime) {
		CurrTime = currTime;
	}
	public WaitlistedPatient getPatient() {
		return patient;
	}
	public void setPatient(WaitlistedPatient patient) {
		this.patient = patient;
	}
	public DeceasedOrgan getOrgan() {
		return organ;
	}
	public void setOrgan(DeceasedOrgan organ) {
		this.organ = organ;
	}
	public String getExit_reason() {
		return exit_reason;
	}
	public void setExit_reason(String exit_reason) {
		this.exit_reason = exit_reason;
	}

	
	
		
}
