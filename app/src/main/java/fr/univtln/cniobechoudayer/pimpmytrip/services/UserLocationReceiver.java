package fr.univtln.cniobechoudayer.pimpmytrip.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class UserLocationReceiver extends ResultReceiver{

    private ResultReceiver mReceiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public UserLocationReceiver(Handler handler) {
        super(handler);
    }

    public ResultReceiver getmReceiver() {
        return mReceiver;
    }

    public void setmReceiver(ResultReceiver mReceiver) {
        this.mReceiver = mReceiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {



    }
}
