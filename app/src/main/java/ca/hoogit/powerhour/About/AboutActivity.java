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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import ca.hoogit.powerhour.BaseActivity;
import ca.hoogit.powerhour.R;
import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.version) TextView mVersion;
    @Bind(R.id.me) LinearLayout mMe;
    @Bind(R.id.source_code) LinearLayout mSource;
    @Bind(R.id.rate) LinearLayout mRate;
    @Bind(R.id.licenses) LinearLayout mLicenses;

    private String mPackageName;

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
        return 0;
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

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            mPackageName = pInfo.packageName;
            mVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            mPackageName = getApplication().getPackageName();
        }
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
                Uri uri = Uri.parse("market://details?id=" + getApplication().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" +
                                    getApplication().getPackageName())));
                }
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
