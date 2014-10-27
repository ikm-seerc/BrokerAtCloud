package org.seerc.brokeratcloud.policycompletenesscompliance;

public class EvaluationReport {

	private String serviceInstance;
	private BrokerPolicyReportObject brokerPolicyReport;
	private CompletenessReportObject completenessReport;
	private ComplianceReportObject complianceReport;
	
	public EvaluationReport() {
		this.brokerPolicyReport = new BrokerPolicyReportObject();
		this.completenessReport = new CompletenessReportObject();
		this.complianceReport = new ComplianceReportObject();
	}
	
	public String getServiceInstance() {
		return serviceInstance;
	}
	public void setServiceInstance(String serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
	public CompletenessReportObject getCompletenessReport() {
		return completenessReport;
	}
	public void setCompletenessReport(CompletenessReportObject completenessReport) {
		this.completenessReport = completenessReport;
	}
	public ComplianceReportObject getComplianceReport() {
		return complianceReport;
	}
	public void setComplianceReport(ComplianceReportObject complianceReport) {
		this.complianceReport = complianceReport;
	}

	public BrokerPolicyReportObject getBrokerPolicyReport() {
		return brokerPolicyReport;
	}

	public void setBrokerPolicyReport(BrokerPolicyReportObject brokerPolicyReport) {
		this.brokerPolicyReport = brokerPolicyReport;
	}
	
	
	
}
