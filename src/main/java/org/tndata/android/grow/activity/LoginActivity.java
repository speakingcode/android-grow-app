package org.tndata.android.grow.activity;

import java.util.ArrayList;

import org.tndata.android.grow.R;
import org.tndata.android.grow.GrowApplication;
import org.tndata.android.grow.fragment.LauncherFragment;
import org.tndata.android.grow.fragment.LauncherFragment.LauncherFragmentListener;
import org.tndata.android.grow.fragment.LoginFragment;
import org.tndata.android.grow.fragment.LoginFragment.LoginFragmentListener;
import org.tndata.android.grow.fragment.SignUpFragment;
import org.tndata.android.grow.fragment.SignUpFragment.SignUpFragmentListener;
import org.tndata.android.grow.fragment.WebFragment;
import org.tndata.android.grow.model.User;
import org.tndata.android.grow.task.LoginTask;
import org.tndata.android.grow.task.LoginTask.LoginTaskListener;
import org.tndata.android.grow.util.Constants;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

public class LoginActivity extends ActionBarActivity implements
        LauncherFragmentListener, SignUpFragmentListener,
        LoginFragmentListener, LoginTaskListener {
    private static final int DEFAULT = 0;
    private static final int LOGIN = 1;
    private static final int SIGN_UP = 2;
    private static final int TERMS = 3;
    private Toolbar mToolbar;
    private WebFragment mWebFragment = null;
    private LauncherFragment mLauncherFragment = null;
    private LoginFragment mLoginFragment = null;
    private SignUpFragment mSignUpFragment = null;
    private ArrayList<Fragment> mFragmentStack = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        swapFragments(DEFAULT, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String email = settings.getString("email", "");
        String password = settings.getString("password", "");
        if (!email.isEmpty() && !password.isEmpty()) {
            logUserIn(email, password);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { // Back key pressed
            handleBackStack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            handleBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleBackStack() {
        if (!mFragmentStack.isEmpty()) {
            Fragment fragment = mFragmentStack.get(mFragmentStack.size() - 1);
            if (fragment instanceof SignUpFragment) {
                if (((SignUpFragment) fragment).isEmailViewShown()) {
                    ((SignUpFragment) fragment).hideEmailView();
                    return;
                }
            }
            mFragmentStack.remove(mFragmentStack.size() - 1);
        }

        if (mFragmentStack.isEmpty()) {
            finish();
        } else {
            Fragment fragment = mFragmentStack.get(mFragmentStack.size() - 1);

            int index = DEFAULT;
            if (fragment instanceof LoginFragment) {
                index = LOGIN;
            } else if (fragment instanceof SignUpFragment) {
                index = SIGN_UP;
            } else if (fragment instanceof WebFragment) {
                index = TERMS;
            }

            swapFragments(index, false);
        }
    }

    private void transitionToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void transitionToOnBoarding() {
        Intent intent = new Intent(getApplicationContext(),
                OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void logUserIn(String emailAddress, String password) {
        User user = new User();
        user.setEmail(emailAddress);
        user.setPassword(password);
        for (Fragment fragment : mFragmentStack) {
            if (fragment instanceof LauncherFragment) {
                ((LauncherFragment) fragment).showProgress(true);
            }
        }
        new LoginTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                user);
    }

    private void swapFragments(int index, boolean addToStack) {
        Fragment fragment = null;
        switch (index) {
        case DEFAULT:
            if (mLauncherFragment == null) {
                mLauncherFragment = new LauncherFragment();
            }
            fragment = mLauncherFragment;
            getSupportActionBar().hide();
            break;
        case LOGIN:
            if (mLoginFragment == null) {
                mLoginFragment = new LoginFragment();
            }
            fragment = mLoginFragment;
            getSupportActionBar().hide();
            break;
        case SIGN_UP:
            if (mSignUpFragment == null) {
                mSignUpFragment = new SignUpFragment();
            }
            fragment = mSignUpFragment;
            getSupportActionBar().hide();
            break;
        case TERMS:
            if (mWebFragment == null) {
                mWebFragment = new WebFragment();
            }
            fragment = mWebFragment;
            getSupportActionBar().show();
            mToolbar.setTitle(R.string.terms_title);
            mWebFragment.setUrl(Constants.TERMS_AND_CONDITIONS_URL);
            break;
        default:
            break;
        }
        if (fragment != null) {
            if (addToStack) {
                mFragmentStack.add(fragment);
            }
            getFragmentManager().beginTransaction()
                    .replace(R.id.base_content, fragment).commit();
        }

    }

    @Override
    public void signUp() {
        swapFragments(SIGN_UP, true);
    }

    @Override
    public void logIn() {
        swapFragments(LOGIN, true);
    }

    @Override
    public void loginSuccess(User user) {
        saveUserInfo(user, false);
    }

    private void saveUserInfo(User user, boolean newUser) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("auth_token", user.getToken());
        editor.putString("first_name", user.getFirstName());
        editor.putString("last_name", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("password", user.getPassword());
        editor.putInt("id", user.getId());

        editor.commit();
        ((GrowApplication) getApplication()).setToken(user.getToken());
        ((GrowApplication) getApplication()).setUser(user);
        if (newUser) {
            transitionToOnBoarding();
        } else {
            transitionToMain();
        }
    }

    @Override
    public void loginResult(User result) {
        for (Fragment fragment : mFragmentStack) {
            if (fragment instanceof LauncherFragment) {
                ((LauncherFragment) fragment).showProgress(false);
            }
        }
        if ((result != null) && (result.getError().isEmpty())) {
            saveUserInfo(result, false);
        } else {
            swapFragments(LOGIN, true);
        }
    }

    @Override
    public void signUpSuccess(User user) {
        saveUserInfo(user, true);
    }

    @Override
    public void showTermsAndConditions() {
        swapFragments(TERMS, true);
    }
}
