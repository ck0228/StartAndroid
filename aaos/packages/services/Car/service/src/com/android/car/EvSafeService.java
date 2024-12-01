package com.android.car.evsafeservice;

import android.car.Car;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.property.CarPropertyEvent;
import android.car.hardware.property.ICarPropertyEventListener;
import android.content.Context;
import android.hardware.automotive.vehicle.V2_0.VehicleArea;
import android.hardware.automotive.vehicle.V2_0.VehicleGear;
import android.hardware.automotive.vehicle.V2_0.VehicleProperty;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import android.util.IndentingPrintWriter;

import com.android.car.CarPropertyService;
import com.android.car.CarServiceBase;

import java.util.List;
/**
 * 차량의 기어 상태를 모니터링하는 서비스
 * CarServiceBase를 구현하여 차량 서비스의 생명주기 관리
 */
public class EvSafeService extends android.car.evsafeservice.IEvSafeService.Stub 
        implements CarServiceBase {

    private static final String TAG = "EvSafeService";
    private static final boolean DBG = true;

    private final Context mContext;
    private final CarPropertyService mPropertyService;
    private final Object mLock = new Object();

    private int mCurrentGear = VehicleGear.GEAR_PARK;

    /**
     * 기어 변경 이벤트를 수신하는 리스너
     * AIDL을 통한 IPC 통신을 위해 Stub 클래스 확장
     */
    private final ICarPropertyEventListener mGearSelectionPropertyListener = 
        new ICarPropertyEventListener.Stub() {
            @Override
            public void onEvent(List<CarPropertyEvent> events) throws RemoteException {
                synchronized (mLock) {
                    handleGearChangeEvent(events.get(events.size() - 1));
                }
            }
    };

    /**
     * 서비스 생성자
     * @param context 애플리케이션 컨텍스트
     * @param propertyService 차량 속성 서비스
     */
    public EvSafeService(Context context, CarPropertyService propertyService) {
        mContext = context;
        mPropertyService = propertyService;
    }

    /**
     * 서비스 초기화
     * 기어 선택 속성 사용 가능 여부 확인 및 리스너 등록
     */
    @Override
    public void init() {
        if (DBG) {
            Slog.d(TAG, "Initializing service");
        }

        // 기어 선택 속성 사용 가능 여부 확인
        if (mPropertyService == null ||
            mPropertyService.getProperty(VehicleProperty.GEAR_SELECTION, 
                VehicleArea.GLOBAL) == null) {
            Slog.e(TAG, "Service disabled - GEAR_SELECTION unavailable");
            return;
        }

        // 기어 변경 이벤트 리스너 등록
        mPropertyService.registerListener(VehicleProperty.GEAR_SELECTION, 0,
                mGearSelectionPropertyListener);
    }

    /**
     * 서비스 해제
     * 등록된 리스너 제거
     */
    @Override
    public void release() {
        synchronized (mLock) {
            if (mPropertyService != null) {
                mPropertyService.unregisterListener(VehicleProperty.GEAR_SELECTION,
                        mGearSelectionPropertyListener);
            }
        }
    }

    /**
     * 기어 변경 이벤트 처리
     * @param event 수신된 차량 속성 이벤트
     */
    private void handleGearChangeEvent(CarPropertyEvent event) {
        // Check event type
        if (event.getEventType() != CarPropertyEvent.PROPERTY_EVENT_PROPERTY_CHANGE) {
            return;
        }

        // Check property ID is gear selection
        CarPropertyValue value = event.getCarPropertyValue();
        if (value.getPropertyId() != VehicleProperty.GEAR_SELECTION) {
            return;
        }

        // Get gear value
        int gear = (Integer) value.getValue();
        synchronized (mLock) {
            mCurrentGear = gear;
            Slog.i(TAG, "Vehicle shifted to: " + gear);
        }
    }

    /**
     * 현재 기어 상태 반환
     * @return 현재 기어 값
     */
    public int getCurrentGear() {
        synchronized (mLock) {
            return mCurrentGear;
        }
    }

    /**
     * 파킹 기어 상태 확인
     * @return 파킹 기어 여부
     */
    public boolean isInPark() {
        synchronized (mLock) {
            return mCurrentGear == VehicleGear.GEAR_PARK;
        }
    }


    @Override
    public void dump(IndentingPrintWriter writer) {
        writer.printf("Current Gear: %d\n", mCurrentGear);
    }
}