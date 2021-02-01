package com.e.serviceproviderapp;

public class JobListData {
    private String jobID, jobTime, jobDate, jobType, jobEarn;

    public String getJobType() {
        return jobType;
    }

    public String getJobID() {
        return jobID;
    }


    public String getJobTime() {
        return jobTime;
    }


    public String getJobDate() {
        return jobDate;
    }

    public String getJobEarn() {
        return jobEarn;
    }


    public JobListData(String jobID, String jobTime, String jobDate, String jobType, String jobEarn) {
        this.jobID = jobID;
        this.jobTime = jobTime;
        this.jobDate = jobDate;
        this.jobType = jobType;
        this.jobEarn = jobEarn;

    }

}
