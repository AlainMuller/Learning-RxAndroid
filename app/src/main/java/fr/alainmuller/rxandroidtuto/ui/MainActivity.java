package fr.alainmuller.rxandroidtuto.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import fr.alainmuller.rxandroidtuto.R;
import fr.alainmuller.rxandroidtuto.data.GitHubMember;
import fr.alainmuller.rxandroidtuto.manager.ApiManager;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;

public class MainActivity extends AppCompatActivity implements Observer<String> {

    private static final String[] GITHUB_MEMBERS = new String[]{"AlainMuller", "JakeWharton", "cyrilmottier", "romainguy", "romannurik", "dwursteisen"};
    private TextView mMembersView;
    private ProgressBar mProgressBar;
    private Subscription mSubscription;
    private long mStartTime;

    // *********************************************************************************************
    // LIFECYCLE
    // *********************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMembersView = (TextView) findViewById(R.id.textview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mStartTime = System.currentTimeMillis();

        /* emits one String (userName) at a time from the constant */
        mSubscription = Observable.from(GITHUB_MEMBERS)
                /* retrieve each GitHubMember based on an API call with the emitted userName*/
                .flatMap(ApiManager::getGitHubMember)
                /* convert each GitHubMember as a String to be displayed */
                .map(GitHubMember::toString)
                /* extra call to log data without altering this chain */
                .doOnEach(title -> Log.d(MainActivity.class.getSimpleName(), "member retrieved : " + title))
                /* aggregate all userNames to one single String */
                .aggregate((s, s2) -> s + "\n" + s2)
                /* handle the items on a separate thread  */
                .subscribeOn(Schedulers.threadPoolForIO())
                /* react to the items on the main thread (we'll update the layout) */
                .observeOn(AndroidSchedulers.mainThread())
                /* MainActivity implements Observer (see onNext) */
                .subscribe(this);
    }

    @Override
    protected void onDestroy() {
        /* unsubscribe to avoid memory leaks */
        mSubscription.unsubscribe();
        super.onDestroy();
    }

    // *********************************************************************************************
    // OBSERVER INTERFACE
    // *********************************************************************************************

    @Override
    public void onCompleted() {
        hideProgressBar();
        Toast.makeText(this, String.format(Locale.getDefault(), "Loading complete : %d items in %d ms", GITHUB_MEMBERS.length, System.currentTimeMillis() - mStartTime), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Throwable e) {
        hideProgressBar();
        Toast.makeText(this, e.getLocalizedMessage() != null && !"".equals(e.getLocalizedMessage()) ? "Error : " + e.getLocalizedMessage() : "Unknown error", Toast.LENGTH_LONG).show();
        Log.e(MainActivity.class.getSimpleName(), "Exception : " + e.getLocalizedMessage());
        e.printStackTrace();
    }

    @Override
    public void onNext(String members) {
        mMembersView.setText(members);
    }

    // *********************************************************************************************
    // PRIVATE METHODS
    // *********************************************************************************************

    private void hideProgressBar() {
        mProgressBar.animate().setDuration(500)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }
}
