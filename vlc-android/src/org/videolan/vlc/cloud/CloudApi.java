package org.videolan.vlc.cloud;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CloudApi implements Callback {

    private final OkHttpClient client;
    private String token;
    private OnTokenReceivedListener listener;


    public CloudApi(OnTokenReceivedListener listener) {
        this.listener = listener;
        client = new OkHttpClient.Builder().cookieJar(new CookieJar() {

            private List<Cookie> cookieStore = new ArrayList<>();

            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                cookieStore.addAll(list);
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                return cookieStore;
            }
        }).build();
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {

    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        ResponseBody contentBody = response.body();
        if (contentBody != null) {
            String contentString = contentBody.string();
            try {
                JSONObject jObject = new JSONObject(contentString);
                JSONObject body = jObject.getJSONObject("body");
                token = body.getString("token");

                listener.onTokenReceived(this);

                Request folderRequest = new Request.Builder()
                        .url("https://cloud.mail.ru/api/v2/folder?token=aGk1VKdhfP1mMUqVnzjQ9RbAqd68i25s&home=/")
                        .get()
                        .build();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getToken() {
        return token;
    }

    private String getResponseText(Response response) throws IOException {
        ResponseBody contentBody = response.body();
        if (contentBody != null) {
            return contentBody.string();
        }
        return "";
    }

    public void requestToken() {
        final Callback callback = this;
        MultipartBody requestBodyAuth = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Login", "tulbaev.dima@mail.ru")
                .addFormDataPart("Password", "9q6a3z8w5s2x")
                .build();

        Request requestAuth = new Request.Builder().url("https://auth.mail.ru/cgi-bin/auth")
                .post(requestBodyAuth)
                .build();


        client.newCall(requestAuth).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                Request requestSdc = new Request.Builder().url("https://auth.mail.ru/sdc?from=https%3A%2F%2Fcloud.mail.ru")
                        .get()
                        .build();

                client.newCall(requestSdc).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Request requestToken = new Request.Builder().url("https://cloud.mail.ru/api/v2/tokens/csrf")
                                .get()
                                .build();

                        client.newCall(requestToken).enqueue(callback);
                    }
                });
            }
        });
    }


    public List<CloudFile> listFilesInDirectory(String dir) throws IOException, JSONException {
        List<CloudFile> list = new ArrayList<>();
        HttpUrl.Builder httpBuilder = HttpUrl.parse("https://cloud.mail.ru/api/v2/folder").newBuilder();
        httpBuilder.addQueryParameter("token", token);
        httpBuilder.addQueryParameter("home", dir);

        Request requestFolder = new Request.Builder().
                url(httpBuilder.build())
                .get()
                .build();


        Response response = client.newCall(requestFolder).execute();

        JSONObject obj = new JSONObject(getResponseText(response));
        JSONObject body = obj.getJSONObject("body");
        JSONArray array = body.getJSONArray("list");

        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            CloudFile file = new CloudFile(o.getString("name"),
                    obj.getString("type"),
                    obj.getString("home"));
            list.add(file);
        }

        return list;

    }

}
