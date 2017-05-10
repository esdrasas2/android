package com.crisnello.notereader.util;

import android.content.Context;



/**
 * Created by crisnello on 10/05/17.
 */

public class Util {

    private Context context;

    public Util(Context pContext){
        context = pContext;

    }

    public void showAlert(String pMsg){
        CustomAlert alert = new CustomAlert(context);
        alert.setMessage(pMsg);
        alert.show();
    }

}
