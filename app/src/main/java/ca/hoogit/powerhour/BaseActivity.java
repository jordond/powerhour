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
package ca.hoogit.powerhour;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhour.Util.StatusBarUtil;

/**
 * @author jordon
 *
 * Date    22/07/15
 * Description 
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Bind(R.id.appBar) Toolbar mToolbar;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            StatusBarUtil.getInstance().init(this);
        }
        if (getEventBusEnabled()) {
            BusProvider.getInstance().register(this);
        }
        if(mToolbar != null){
            mToolbar.setBackgroundColor(getToolbarColor());
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(getDisplayHomeAsUpEnabled());
        } else {
            throw new NullPointerException("Layout must contain a toolbar with id 'appBar'");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuResource() != 0) {
            getMenuInflater().inflate(getMenuResource(), menu);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getEventBusEnabled()) {
            BusProvider.getInstance().unregister(this);
        }
    }

    protected abstract int getToolbarColor();

    protected abstract int getLayoutResource();

    protected abstract int getMenuResource();

    protected abstract boolean getDisplayHomeAsUpEnabled();

    protected  abstract boolean getEventBusEnabled();

    protected Toolbar getToolbar(){
        return this.mToolbar;
    }
}
