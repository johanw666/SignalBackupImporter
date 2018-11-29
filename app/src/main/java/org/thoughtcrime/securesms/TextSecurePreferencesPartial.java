package org.thoughtcrime.securesms;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class TextSecurePreferencesPartial {

    private static final String TAG = TextSecurePreferencesPartial.class.getSimpleName();

    private static final String DATABASE_ENCRYPTED_SECRET     = "pref_database_encrypted_secret";
    private static final String DATABASE_UNENCRYPTED_SECRET   = "pref_database_unencrypted_secret";
    private static final String ATTACHMENT_ENCRYPTED_SECRET   = "pref_attachment_encrypted_secret";
    private static final String ATTACHMENT_UNENCRYPTED_SECRET = "pref_attachment_unencrypted_secret";
    private static final String LOG_ENCRYPTED_SECRET   = "pref_log_encrypted_secret";
    private static final String LOG_UNENCRYPTED_SECRET = "pref_log_unencrypted_secret";

    public static void setDatabaseEncryptedSecret(@NonNull Context context, @NonNull String secret) {
        Log.d(TAG, "Storing encypted secret: " + secret);
        setStringPreference(context, DATABASE_ENCRYPTED_SECRET, secret);
    }

    public static void setDatabaseUnencryptedSecret(@NonNull Context context, @Nullable String secret) {
        setStringPreference(context, DATABASE_UNENCRYPTED_SECRET, secret);
    }

    public static @Nullable String getDatabaseUnencryptedSecret(@NonNull Context context) {
        return getStringPreference(context, DATABASE_UNENCRYPTED_SECRET, null);
    }

    public static @Nullable String getDatabaseEncryptedSecret(@NonNull Context context) {
        return getStringPreference(context, DATABASE_ENCRYPTED_SECRET, null);
    }

    public static void setStringPreference(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static String getStringPreference(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static void setAttachmentEncryptedSecret(@NonNull Context context, @NonNull String secret) {
        setStringPreference(context, ATTACHMENT_ENCRYPTED_SECRET, secret);
    }

    public static void setAttachmentUnencryptedSecret(@NonNull Context context, @Nullable String secret) {
        setStringPreference(context, ATTACHMENT_UNENCRYPTED_SECRET, secret);
    }

    public static @Nullable String getAttachmentEncryptedSecret(@NonNull Context context) {
        return getStringPreference(context, ATTACHMENT_ENCRYPTED_SECRET, null);
    }

    public static @Nullable String getAttachmentUnencryptedSecret(@NonNull Context context) {
        return getStringPreference(context, ATTACHMENT_UNENCRYPTED_SECRET, null);
    }

    public static void setLogEncryptedSecret(Context context, String base64Secret) {
        setStringPreference(context, LOG_ENCRYPTED_SECRET, base64Secret);
    }

    public static String getLogEncryptedSecret(Context context) {
        return getStringPreference(context, LOG_ENCRYPTED_SECRET, null);
    }

    public static void setLogUnencryptedSecret(Context context, String base64Secret) {
        setStringPreference(context, LOG_UNENCRYPTED_SECRET, base64Secret);
    }

    public static String getLogUnencryptedSecret(Context context) {
        return getStringPreference(context, LOG_UNENCRYPTED_SECRET, null);
    }
}
