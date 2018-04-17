package net.elfak.tele.data;

import android.content.Context;

import net.elfak.tele.data.presenter.Presenter;

/**
 * Created by milosjovac on 6/13/16.
 */
public class GlobalBank {
    private static GlobalBank INSTANCE;
    public Context context;
    public Presenter presenter;

    private GlobalBank(Context context){
        this.context = context;
        this.presenter = Presenter.createInstance(this);
    }

    public static void createInstance(Context applicationContext) {
        INSTANCE = new GlobalBank(applicationContext);
    }

    public static GlobalBank getInstance(){
        return INSTANCE;
    }


}
