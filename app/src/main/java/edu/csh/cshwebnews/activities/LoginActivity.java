package edu.csh.cshwebnews.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.fragments.AuthFragment;
import mehdi.sakout.fancybuttons.FancyButton;


public class LoginActivity extends FragmentActivity {

    FancyButton button;
    TextView webNewsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        button      = (FancyButton) findViewById(R.id.btn_login);
        webNewsText = (TextView) findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAuthDialog();
            }
        });

        introAnimation();

        // Stetho for debugging
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    /**
     * Displays the OAuth dialog.
     */
    void showAuthDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("AuthFragment");

        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        AuthFragment authFragment = new AuthFragment();
        authFragment.show(ft, "AuthFragment");
    }

    /**
     * Displays the splash screen animation.
     */
    void introAnimation() {
        button.setVisibility(View.GONE);
        webNewsText.setVisibility(View.GONE);
        Animation anim = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                button.setVisibility(View.VISIBLE);
                webNewsText.setVisibility(View.VISIBLE);
                Animation animFade = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                button.startAnimation(animFade);
                webNewsText.startAnimation(animFade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        ImageView logo = (ImageView) findViewById(R.id.image_logo);
        logo.startAnimation(anim);
    }
}
