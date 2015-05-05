package edu.csh.cshwebnews.models;

import com.google.gson.annotations.SerializedName;

public class AccessToken {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        if(! Character.isUpperCase(tokenType.charAt(0))){
            tokenType = Character.toString(tokenType.charAt(0)).toUpperCase() +
                    tokenType.substring(1);
        }
        return tokenType;
    }

}
