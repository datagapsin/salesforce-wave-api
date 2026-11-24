package com.springml.salesforce.wave.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class BatchInfoList implements Serializable {
    private static final long serialVersionUID = -6396097094521689813L;

    private List<BatchInfo> batchInfo;

    public List<BatchInfo> getBatchInfo() {
        return batchInfo;
    }

    public void setBatchInfo(List<BatchInfo> batchInfo) {
        this.batchInfo = batchInfo;
    }

}
