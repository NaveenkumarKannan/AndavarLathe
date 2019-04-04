package com.androfocus.location.tracking.andavaruser;

public class AssignWorkData {
    String work_title,details,deadline,fLetter,work_type,collection_amt,companyName,companyAddr,work_id;

    public AssignWorkData(String work_title, String details, String deadline, String fLetter, String work_type, String collection_amt, String companyName, String companyAddr, String work_id) {
        this.work_title = work_title;
        this.details = details;
        this.deadline = deadline;
        this.fLetter = fLetter;
        this.work_type = work_type;
        this.collection_amt = collection_amt;
        this.companyName = companyName;
        this.companyAddr = companyAddr;
        this.work_id = work_id;
    }

    public String getWork_id() {
        return work_id;
    }

    public void setWork_id(String work_id) {
        this.work_id = work_id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddr() {
        return companyAddr;
    }

    public void setCompanyAddr(String companyAddr) {
        this.companyAddr = companyAddr;
    }

    public String getWork_type() {
        return work_type;
    }

    public void setWork_type(String work_type) {
        this.work_type = work_type;
    }

    public String getCollection_amt() {
        return collection_amt;
    }

    public void setCollection_amt(String collection_amt) {
        this.collection_amt = collection_amt;
    }

    public String getWork_title() {
        return work_title;
    }

    public void setWork_title(String work_title) {
        this.work_title = work_title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getfLetter() {
        return fLetter;
    }

    public void setfLetter(String fLetter) {
        this.fLetter = fLetter;
    }
}
