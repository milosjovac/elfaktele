package net.elfak.tele;

import android.app.Application;
import android.content.Context;
import android.os.UserManager;

import com.crashlytics.android.Crashlytics;

import net.elfak.tele.data.GlobalBank;

import java.lang.reflect.Method;

import io.fabric.sdk.android.Fabric;

/**
 * Created by milosjovac on 6/13/16.
 */
public class ElfakApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        GlobalBank.createInstance(getApplicationContext());
        GlobalBank.getInstance().presenter.init();

        // fix android leak fix which is caused by UserManager holding on to a activity ctx
        try {
            final Method m = UserManager.class.getMethod("get", Context.class);
            m.setAccessible(true);
            m.invoke(null, this);

            //above is reflection for below...
            //UserManager.get();
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            }
        }
    }
}