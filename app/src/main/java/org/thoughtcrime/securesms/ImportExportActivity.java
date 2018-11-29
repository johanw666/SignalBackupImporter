package org.thoughtcrime.securesms;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ImportExportActivity extends AppCompatActivity  {
    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private static final int SUCCESS    = 0;
    private static final int NO_SD_CARD = 1;
    private static final int ERROR_IO   = 2;

    final private int REQUEST_CODE_IMPORT_ASK_PERMISSIONS = 10;
    final private int REQUEST_CODE_EXPORT_ASK_PERMISSIONS = 11;

    private String WRITE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private TextView appVersion;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
        appVersion = (TextView)findViewById(R.id.app_version);

        Button importButton = (Button)findViewById(R.id.button_import);
        importButton.setOnClickListener(importOnClickListener);

        Button exportButton = (Button)findViewById(R.id.button_export);
        exportButton.setOnClickListener(exportOnClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVersionString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    void setVersionString() {
        String version = "Unknown";
        String versionNameAndNumber;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNameAndNumber = getApplicationContext().getResources().getString(R.string.app_name) + " version " + version;
        appVersion.setText(versionNameAndNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_IMPORT_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //importEncryptedBackup();
                    handleImportEncryptedBackup();
                } else {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_EXPORT_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //exportEncryptedBackup();
                    handleExportEncryptedBackup();
                } else {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private View.OnClickListener exportOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if we have WRITE_EXTERNAL_STORAGE permission
                int hasWriteContactsPermission = checkSelfPermission(WRITE_PERMISSION);
                if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_PERMISSION}, REQUEST_CODE_EXPORT_ASK_PERMISSIONS);
                    return;
                }
            }
            handleExportEncryptedBackup();
        }
    };
    private View.OnClickListener importOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if we have WRITE_EXTERNAL_STORAGE permission
                int hasWriteContactsPermission = checkSelfPermission(WRITE_PERMISSION);
                if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{WRITE_PERMISSION}, REQUEST_CODE_IMPORT_ASK_PERMISSIONS);
                    return;
                }
            }
            handleImportEncryptedBackup();
        }
    };

    private void handleImportEncryptedBackup() {
        Log.d(TAG, "handleImportEncryptedBackup");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIconAttribute(R.attr.dialog_alert_icon);
        builder.setTitle(getString(R.string.ImportFragment_restore_encrypted_backup));
        builder.setMessage(getString(R.string.ImportFragment_restoring_an_encrypted_backup_will_completely_replace_your_existing_keys));
        builder.setPositiveButton(getString(R.string.ImportFragment_restore), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ImportEncryptedBackupTask().execute();
            }
        });
        builder.setNegativeButton(getString(R.string.ImportFragment_cancel), null);
        builder.show();
    }

    private class ImportEncryptedBackupTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "ImportEncryptedBackupTask:onPreExecute");
            progressDialog = ProgressDialog.show(ImportExportActivity.this,
                    getString(R.string.ImportFragment_restoring),
                    getString(R.string.ImportFragment_restoring_encrypted_backup),
                    true, false);
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "ImportEncryptedBackupTask:onPostExecute");
            Context context = ImportExportActivity.this;

            if (progressDialog != null)
                progressDialog.dismiss();

            if (context == null)
                return;

            switch (result) {
                case NO_SD_CARD:
                    Toast.makeText(context,
                            context.getString(R.string.ImportFragment_no_encrypted_backup_found),
                            Toast.LENGTH_LONG).show();
                    break;
                case ERROR_IO:
                    Toast.makeText(context,
                            context.getString(R.string.ImportFragment_error_importing_backup),
                            Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    Toast.makeText(context,
                            context.getString(R.string.importing_complete),
                            Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "ImportEncryptedBackupTask:doInBackground");
            try {
                EncryptedBackupExporter.importFromSd(ImportExportActivity.this);
                return SUCCESS;
            } catch (NoExternalStorageException e) {
                Log.w("ImportFragment", e);
                return NO_SD_CARD;
            } catch (IOException e) {
                Log.w("ImportFragment", e);
                return ERROR_IO;
            }
        }
    }

    private void handleExportEncryptedBackup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIconAttribute(R.attr.dialog_info_icon);
        builder.setTitle(getString(R.string.ExportFragment_export_to_sd_card));
        builder.setMessage(getString(R.string.ExportFragment_this_will_export_your_encrypted_keys_settings_and_messages));
        builder.setPositiveButton(getString(R.string.ExportFragment_export), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ExportEncryptedTask().execute();
            }
        });
        builder.setNegativeButton(getString(R.string.ExportFragment_cancel), null);
        builder.show();
    }

    private class ExportEncryptedTask extends AsyncTask<Void, Void, Integer> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ImportExportActivity.this,
                    getString(R.string.ExportFragment_exporting),
                    getString(R.string.ExportFragment_exporting_keys_settings_and_messages),
                    true, false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            Context context = ImportExportActivity.this;

            if (dialog != null) dialog.dismiss();

            if (context == null) return;

            switch (result) {
                case NO_SD_CARD:
                    Toast.makeText(context,
                            context.getString(R.string.ExportFragment_error_unable_to_write_to_storage),
                            Toast.LENGTH_LONG).show();
                    break;
                case ERROR_IO:
                    Toast.makeText(context,
                            context.getString(R.string.ExportFragment_error_while_writing_to_storage),
                            Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    Toast.makeText(context,
                            context.getString(R.string.ExportFragment_export_successful),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                EncryptedBackupExporter.exportToSd(ImportExportActivity.this);
                return SUCCESS;
            } catch (NoExternalStorageException e) {
                Log.w("ExportFragment", e);
                return NO_SD_CARD;
            } catch (IOException e) {
                Log.w("ExportFragment", e);
                return ERROR_IO;
            }
        }
    }

}
