package com.oxilo.barcode.Pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by oxiloindia on 2/2/2016.
 */

public class CustomRequest implements Parcelable{

    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("instance_url")
    @Expose
    private String instanceUrl;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("token_type")
    @Expose
    private String tokenType;
    @SerializedName("issued_at")
    @Expose
    private String issuedAt;
    @SerializedName("signature")
    @Expose
    private String signature;

    private String email;
    private String password;

    private String clientSecret;
    private String clientId;
    private String securityToken;
    private double latitude;

    private double longitude;

    private boolean isCameraOpenFirstTime=false;

    public CustomRequest(){

    }

    public CustomRequest(Parcel in) {
        accessToken = in.readString();
        instanceUrl = in.readString();
        id = in.readString();
        tokenType = in.readString();
        issuedAt = in.readString();
        signature = in.readString();
        email = in.readString();
        password = in.readString();
        clientSecret = in.readString();
        clientId = in.readString();
        securityToken = in.readString();
        isCameraOpenFirstTime = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<CustomRequest> CREATOR = new Creator<CustomRequest>() {
        @Override
        public CustomRequest createFromParcel(Parcel in) {
            return new CustomRequest(in);
        }

        @Override
        public CustomRequest[] newArray(int size) {
            return new CustomRequest[size];
        }
    };

    /**
     *
     * @return
     * The accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     *
     * @param accessToken
     * The access_token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     *
     * @return
     * The instanceUrl
     */
    public String getInstanceUrl() {
        return instanceUrl;
    }

    /**
     *
     * @param instanceUrl
     * The instance_url
     */
    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The tokenType
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     *
     * @param tokenType
     * The token_type
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     *
     * @return
     * The issuedAt
     */
    public String getIssuedAt() {
        return issuedAt;
    }

    /**
     *
     * @param issuedAt
     * The issued_at
     */
    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    /**
     *
     * @return
     * The signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     *
     * @param signature
     * The signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public boolean isCameraOpenFirstTime() {
        return isCameraOpenFirstTime;
    }

    public void setCameraOpenFirstTime(boolean cameraOpenFirstTime) {
        isCameraOpenFirstTime = cameraOpenFirstTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(instanceUrl);
        dest.writeString(id);
        dest.writeString(tokenType);
        dest.writeString(issuedAt);
        dest.writeString(signature);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(clientSecret);
        dest.writeString(clientId);
        dest.writeString(securityToken);
        dest.writeByte((byte) (isCameraOpenFirstTime ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
