package net.elfak.tele.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.tale.prettysharedpreferences.BooleanEditor;
import com.tale.prettysharedpreferences.PrettySharedPreferences;

import net.elfak.tele.data.GlobalBank;

/**
 * Created by milosjovac on 6/13/16.
 */
public class PrefManager extends PrettySharedPreferences<PrefManager> {
    private static PrefManager INSTANCE;
    private static final String SERVICE_STOPPED_MANUALLY = "SERVICE_STOPPED_MANUALLY";


    public PrefManager(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    public static PrefManager getInstance() {
        if (INSTANCE == null) {
            Context context = GlobalBank.getInstance().context;
            INSTANCE = new PrefManager(context.getSharedPreferences(context.getPackageName(),
                    Context.MODE_PRIVATE));
        }
        return INSTANCE;
    }

    public BooleanEditor<PrefManager> serviceStoppedManually() {
        return getBooleanEditor(SERVICE_STOPPED_MANUALLY);
    }
}
