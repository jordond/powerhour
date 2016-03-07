/*
 * Copyright (C) 2015, Jordon de Hoog
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ca.hoogit.powerhour.About;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import butterknife.Bind;
import ca.hoogit.powerhour.BaseActivity;
import ca.hoogit.powerhour.BuildConfig;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.PowerHourUtils;
import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AboutActivity.class.getSimpleName();

    @Bind(R.id.version) TextView mVersion;
    @Bind(R.id.me) LinearLayout mMe;
    @Bind(R.id.source_code) LinearLayout mSource;
    @Bind(R.id.rate) LinearLayout mRate;
    @Bind(R.id.licenses) LinearLayout mLicenses;

    @Override
    protected int getToolbarColor() {
        return getResources().getColor(R.color.primary);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.menu_about;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected boolean getEventBusEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMe.setOnClickListener(this);
        mSource.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mLicenses.setOnClickListener(this);

        String versionName = "v" + BuildConfig.VERSION_NAME +
                "\nAPK Built\n" + PowerHourUtils.epochToFromNow(BuildConfig.BuildDate);
        mVersion.setText(versionName);

        Answers.getInstance().logCustom(new CustomEvent("Viewed About"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.me:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.developer_url))));
                break;
            case R.id.source_code:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.source_code_url))));
                break;
            case R.id.rate:
                PowerHourUtils.rateApp(getApplication());
                break;
            case R.id.licenses:
                new LicensesDialog.Builder(this)
                        .setNotices(R.raw.notices)
                        .setThemeResourceId(R.style.ToolBarLight_Popup)
                        .build()
                        .show();
                break;
        }
    }
}
