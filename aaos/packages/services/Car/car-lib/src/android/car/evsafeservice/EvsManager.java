package android.car.evsafeservice;

import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.annotation.Hide;
import android.car.Car;
import android.car.CarManagerBase;
import android.car.annotation.RequiredFeature;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 차량 기어 상태 모니터링을 위한 매니저 클래스
 * @hide
 */
@Hide
@RequiredFeature(Car.PROPERTY_SERVICE)
public final class EvsManager extends CarManagerBase {
    private static final String TAG = EvsManager.class.getSimpleName();
    private static final boolean DBG = Log.isLoggable(TAG, Log.DEBUG);

    private final @NonNull IEvSafeService mService;

    /** @hide */
    public EvsManager(@NonNull Car car, @NonNull IBinder service) {
        super(car);
        mService = IEvSafeService.Stub.asInterface(service);
    }

    /**
     * 현재 기어 상태 조회
     * @return 현재 기어 값
     * @hide
     */
    @RequiresPermission(Car.PERMISSION_POWERTRAIN)
    public int getCurrentGear() {
        try {
            return mService.getCurrentGear();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get current gear", e);
        }
    }

    /**
     * 파킹 기어 상태 확인
     * @return 파킹 기어 여부
     * @hide
     */
    @RequiresPermission(Car.PERMISSION_POWERTRAIN)
    public boolean isInPark() {
        try {
            return mService.isInPark();
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to check park state", e);
        }
    }

    @Override
    public void onCarDisconnected() {
        // Clean up resources if needed
    }
}
