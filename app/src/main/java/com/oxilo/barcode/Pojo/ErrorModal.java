package com.oxilo.barcode.Pojo;

/**
 * Created by ericbasendra on 09/02/16.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorModal {

    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("error_description")
    @Expose
    private String errorDescription;

    /**
     *
     * @return
     * The error
     */
    public String getError() {
        return error;
    }

    /**
     *
     * @param error
     * The error
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     *
     * @return
     * The errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     *
     * @param errorDescription
     * The error_description
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}


