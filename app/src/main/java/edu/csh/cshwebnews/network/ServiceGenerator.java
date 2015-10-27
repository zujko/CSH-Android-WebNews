package edu.csh.cshwebnews.network;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class ServiceGenerator {

    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      final String accessToken, final String tokenType) {

        /* Add a StethoInterceptor for debugging */
        OkHttpClient okHttpClient = new OkHttpClient();

        //Non OAuth request, add headers
        if (accessToken != null && tokenType != null) {
            okHttpClient.networkInterceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Request newRequest = request.newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Accept", "application/vnd.csh.webnews.v1+json")
                            .addHeader("Authorization", tokenType + " " + accessToken)
                            .build();

                    return chain.proceed(newRequest);
                }
            });
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();


        return retrofit.create(serviceClass);
    }
}
