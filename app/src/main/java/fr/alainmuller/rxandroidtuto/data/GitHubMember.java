package fr.alainmuller.rxandroidtuto.data;

import java.util.Locale;

public class GitHubMember {
    public String login;
    public int followers;

    @Override
    public String toString() {
        return String.format(Locale.US, "%s: %d followers", login, followers);
    }
}