package com.example.cerberus.TwitterServices.Responses;

public class CheckUploadResponse {
    public ProcessingInfo processing_info;

    public class ProcessingInfo {
        public String state;
        public Integer check_after_secs;
    }
}
