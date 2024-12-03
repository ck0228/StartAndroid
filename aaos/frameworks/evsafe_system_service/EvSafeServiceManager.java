package android.app;
import android.os.RemoteException;
import android.annotation.SystemService;
import android.content.Context;

@SystemService(Context.EVSAFE_SERVICE)
public final class EvSafeServiceManager {
    private final Context mContext;
    private final IEvSafeService mService;

    /**
     * @hide
     */
    EvSafeServiceManager(Context context, IEvSafeService service) {
        mContext = context;
        mService = service;
    }

    /**
     * @hide
     */
    public void printEvSafe() {
        try {
            mService.printEvSafe();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}