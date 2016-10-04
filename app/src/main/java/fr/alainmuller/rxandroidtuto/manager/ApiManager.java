package fr.alainmuller.rxandroidtuto.manager;

import fr.alainmuller.rxandroidtuto.data.GitHubMember;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

public class ApiManager {

    // *********************************************************************************************
    // INTERFACE
    // *********************************************************************************************
    private interface ApiManagerService {
        @GET("/users/{username}")
        GitHubMember getMember(@Path("username") String username);
    }

    // *********************************************************************************************
    // ADAPTER
    // *********************************************************************************************

    private static final RestAdapter restAdapter = new RestAdapter.Builder()
            .setServer("https://api.github.com")
            .build();

    private static final ApiManagerService apiManager = restAdapter.create(ApiManagerService.class);

    public static Observable<GitHubMember> getGitHubMember(final String userName) {
        return Observable.just(apiManager.getMember(userName));
    }
}