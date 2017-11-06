package com.isaac.smartdrawer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class SettingsActivity extends AppCompatActivity {
    public static final String PREF_ADDR = "addr";
    public static final String PREF_SYNC = "background_sync";
    public static final String PREF_ALERT = "alert";
    public static final String PREF_USER_NAME = "user_name";

    private static final String TAG_ADDR = "addr_dialog";

    @BindView(R.id.activity_settings_addr_tv) TextView mAddrTV;
    @BindView(R.id.activity_settings_sync_sw) SwitchCompat mSyncSW;
    @BindView(R.id.activity_settings_smart_alert_sw) SwitchCompat mAlertSW;
    @BindView(R.id.activity_settings_toolbar) Toolbar mToolbar;

    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setTitle("设置");
        setSupportActionBar(mToolbar);
        mAddrTV.setText(getSharedPreferences(PREF_ADDR, MODE_PRIVATE).getString(PREF_ADDR, "192.168.31.76:21567"));
        mSyncSW.setChecked(getSharedPreferences(PREF_SYNC, MODE_PRIVATE).getBoolean(PREF_SYNC, false));
        mAlertSW.setChecked(getSharedPreferences(PREF_ALERT, MODE_PRIVATE).getBoolean(PREF_ALERT, false));

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static String getUserNmae(Context context) {
        return context.getSharedPreferences(PREF_USER_NAME, MODE_PRIVATE).getString(PREF_USER_NAME, "用户名");
    }

    public static void setAddr(Context context, String addr) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_ADDR,MODE_PRIVATE).edit();
        editor.putString(PREF_ADDR, addr);
        editor.apply();
        Client.setConnection(addr);
    }

    public static boolean canSync(Context context) {
        return context.getSharedPreferences(PREF_SYNC, MODE_PRIVATE).getBoolean(PREF_SYNC, false);
    }

    public static boolean canAlert(Context context) {
        return context.getSharedPreferences(PREF_ALERT, MODE_PRIVATE).getBoolean(PREF_ALERT, false);
    }

    public static void setUserName(Context context, String name) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_USER_NAME, MODE_PRIVATE).edit();
        editor.putString(PREF_USER_NAME, name);
        editor.apply();
    }

    @OnClick({R.id.activity_settings_addr, R.id.activity_settings_sync, R.id.activity_settings_smart_alert})
    void onItemClicked(View view) {
        switch (view.getId()) {
            case R.id.activity_settings_addr:
                FragmentManager fm = getSupportFragmentManager();
                AddrDialog dialog = AddrDialog.newInstance(mAddrTV.getText().toString());
                dialog.show(fm, TAG_ADDR);
                break;
            case R.id.activity_settings_sync:
                mSyncSW.setChecked(!mSyncSW.isChecked());
                break;
            case R.id.activity_settings_smart_alert:
                mAlertSW.setChecked(!mAlertSW.isChecked());
        }
    }

    @OnCheckedChanged(R.id.activity_settings_sync_sw)
    void onSyncSWChanged(boolean isChecked) {
                SyncService.setServiceAlarm(this, isChecked);
                SharedPreferences.Editor editor = getSharedPreferences(PREF_SYNC, MODE_PRIVATE).edit();
                editor.putBoolean(PREF_SYNC, isChecked);
                editor.apply();
    }

    @OnCheckedChanged(R.id.activity_settings_smart_alert_sw)
    void onAlertSWChanged(boolean isChecked) {
        SyncService.setSmartAlert(this, isChecked);
        SharedPreferences.Editor editor = getSharedPreferences(PREF_ALERT, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_ALERT, isChecked);
        editor.apply();
    }

    public void onDialogResult(String dialogType, Intent data) {
        switch (dialogType) {
            case AddrDialog.TYPE:
                String addr = data.getStringExtra(AddrDialog.EXTRA_ADDR);
                if (addr.equals(getSharedPreferences(PREF_ADDR, MODE_PRIVATE).getString(PREF_ADDR, "")))
                    break;
                mAddrTV.setText(addr);
                setAddr(this, addr);
                break;
        }
    }

    public static class AddrDialog extends DialogFragment {
        public static final String TYPE = "AddrDialog";

        private static final String ARG_ADDR = "addr_host";

        public static final String EXTRA_ADDR = "com.isaac.smartdrawer.addr";

        @BindView(R.id.dialog_addr_port) TextView mPortTV;
        @BindView(R.id.dialog_addr_host) TextView mHostTV;

        public static AddrDialog newInstance(String addr) {
            AddrDialog dialog = new AddrDialog();
            Bundle bundle = new Bundle();
            bundle.putString(ARG_ADDR, addr);
            dialog.setArguments(bundle);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_settings_addr, null);

            ButterKnife.bind(this, v);

            String[] addr;
            String argString = getArguments().getString(ARG_ADDR);
            if (argString == null || !argString.contains(":")) addr = new String[]{"", ""};
            else addr = argString.split(":");

            mHostTV.setText(addr[0]);
            mPortTV.setText(addr[1]);

            return new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle("地址:端口号")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_ADDR, mHostTV.getText()+":"+mPortTV.getText());
                            ((SettingsActivity) getActivity()).onDialogResult(TYPE, intent);
                        }
                    })
                    .create();

        }
    }
}
