package org.thoughtcrime.securesms;


import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import org.thoughtcrime.securesms.TextSecurePreferencesPartial;

import java.io.IOException;
import java.security.SecureRandom;

public class DatabaseSecretProvider {

  @SuppressWarnings("unused")
  private static final String TAG = DatabaseSecretProvider.class.getSimpleName();

  private final Context context;

  public DatabaseSecretProvider(@NonNull Context context) {
    this.context = context.getApplicationContext();
  }

  public DatabaseSecret getOrCreateDatabaseSecret() {
    String unencryptedSecret = TextSecurePreferencesPartial.getDatabaseUnencryptedSecret(context);
    String encryptedSecret   = TextSecurePreferencesPartial.getDatabaseEncryptedSecret(context);

    if      (unencryptedSecret != null) return getUnencryptedDatabaseSecret(context, unencryptedSecret);
    else if (encryptedSecret != null)   return getEncryptedDatabaseSecret(encryptedSecret);
    else                                return createAndStoreDatabaseSecret(context);
  }

  private DatabaseSecret getUnencryptedDatabaseSecret(@NonNull Context context, @NonNull String unencryptedSecret)
  {
    try {
      DatabaseSecret databaseSecret = new DatabaseSecret(unencryptedSecret);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return databaseSecret;
      } else {
        KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());

        TextSecurePreferencesPartial.setDatabaseEncryptedSecret(context, encryptedSecret.serialize());
        TextSecurePreferencesPartial.setDatabaseUnencryptedSecret(context, null);

        return databaseSecret;
      }
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private DatabaseSecret getEncryptedDatabaseSecret(@NonNull String serializedEncryptedSecret) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      throw new AssertionError("OS downgrade not supported. KeyStore sealed data exists on platform < M!");
    } else {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.SealedData.fromString(serializedEncryptedSecret);
      return new DatabaseSecret(KeyStoreHelper.unseal(encryptedSecret));
    }
  }

  private DatabaseSecret createAndStoreDatabaseSecret(@NonNull Context context) {
    SecureRandom random = new SecureRandom();
    byte[]       secret = new byte[32];
    random.nextBytes(secret);

    DatabaseSecret databaseSecret = new DatabaseSecret(secret);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());
      TextSecurePreferencesPartial.setDatabaseEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferencesPartial.setDatabaseUnencryptedSecret(context, databaseSecret.asString());
    }

    return databaseSecret;
  }

  // JW: store an existing DatabaseSecret. Used for restore of an encrypted backup
  public DatabaseSecret storeOrOverwriteDatabaseSecret(@NonNull Context context, @NonNull DatabaseSecret databaseSecret) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Log.d(TAG, "Storing database secret: " + databaseSecret.asString());
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());
      TextSecurePreferencesPartial.setDatabaseEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferencesPartial.setDatabaseUnencryptedSecret(context, databaseSecret.asString());
    }

    return databaseSecret;
  }
}
