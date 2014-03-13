package at.yawk.fimfiction.android;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import at.yawk.fimfiction.core.SessionActions;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Activity that is opened on launch. Will log in if necessary, might ask the user for credentials. After login is
 * complete, opens the unread display.
 *
 * @author Jonas Konrad (yawkat)
 */
public class LoginActivity extends Fimtivity {
    static boolean attemptedLogin = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        boolean allowAutoLogin = getIntent().getBooleanExtra("autoLogin", true);
        String username = helper().getPreferences().getString("username", null);
        String password = helper().getPreferences().getString("password", null);
        if (username != null) {
            ((TextView) findViewById(R.id.username)).setText(username);
        }
        if (password != null) {
            ((TextView) findViewById(R.id.password)).setText(password);
        }
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((TextView) findViewById(R.id.username)).getText().toString();
                String password = ((TextView) findViewById(R.id.password)).getText().toString();
                boolean save = ((CheckBox) findViewById(R.id.remember)).isChecked();
                login(username, password, save);
            }
        });
        if (allowAutoLogin) {
            if (attemptedLogin) { // already logged in
                skipToRootSearch();
            } else {
                if (username != null && password != null) {
                    login(username, password, false);
                }
            }
        }
    }

    private void login(final String username, final String password, final boolean save) {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setIndeterminate(true);
        loading.setCancelable(false);
        loading.show();
        helper().executeTask(new Runnable() {
            @Override
            public void run() {
                final AtomicReference<SessionActions.LoginStatus> status =
                        new AtomicReference<SessionActions.LoginStatus>();
                while (true) {
                    if (Thread.currentThread().isInterrupted()) { return; }
                    try {
                        status.set(SessionActions.login(helper().getSession().getHttpClient(), username, password));
                        break;
                    } catch (final IOException e) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,
                                               "Could not login (" + e.toString() + "), retrying...",
                                               Toast.LENGTH_SHORT);
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) {
                            return;
                        }
                    }
                }
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.hide();
                        if (status.get() == SessionActions.LoginStatus.SUCCESSFUL) {
                            attemptedLogin = true;
                            if (save) {
                                helper().getPreferences()
                                        .edit()
                                        .putString("username", username)
                                        .putString("password", password)
                                        .commit();
                            }
                            skipToRootSearch();
                        } else {
                            int message = 0;
                            switch (status.get()) {
                            case INVALID_PASSWORD:
                                message = R.string.login_invalid_password;
                                break;
                            case INVALID_USERNAME:
                                message = R.string.login_invalid_username;
                                break;
                            case UNKNOWN:
                                message = R.string.login_unknown;
                                break;
                            }
                            Dialog dialog = new Dialog(LoginActivity.this);
                            dialog.setTitle(message);
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();
                        }
                    }
                });
            }
        });
    }

    private void skipToRootSearch() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, StoryList.class);
        helper().openActivity(intent, true);
    }
}
