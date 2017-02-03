package com.astapley.raspcarandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.astapley.raspcarandroid.api.ApiDrive;
import com.astapley.raspcarandroid.api.RaspCarHelper;
import com.astapley.raspcarandroid.api.RaspCarService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.jakewharton.rxbinding.widget.RxSeekBar.changeEvents;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class DriveActivity extends AppCompatActivity {

    private static final int RANGE = 100;
    private static final int IDLE = 50;
    private static final int THROTTLE_OFFSET = 97;
    private static final int STEERING_OFFSET = 102;

    @BindView(R.id.throttle_seek_bar) SeekBar throttleSeekBar;
    @BindView(R.id.steering_seek_bar) SeekBar steeringSeekBar;
    @BindView(R.id.response_text_view) TextView responseTextView;

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeDisposable disposables = new CompositeDisposable();
    private RaspCarService raspCarService;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        ButterKnife.bind(this);
        raspCarService = RaspCarHelper.getRaspCarService();
        initThrottle();
        initSteering();
    }

    @Override protected void onPause() {
        super.onPause();
        resetCar();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
        disposables.clear();
    }

    @OnClick(R.id.stop_button) void onStopClicked() {
        resetCar();
    }

    private void initThrottle() {
        subscriptions.add(changeEvents(throttleSeekBar)
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(changeEvent -> calculateChange()));
    }

    private void initSteering() {
        subscriptions.add(changeEvents(steeringSeekBar)
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(changeEvent -> calculateChange()));
    }

    private void calculateChange() {
        int throttle = throttleSeekBar.getProgress() + THROTTLE_OFFSET;
        int steering = (RANGE - steeringSeekBar.getProgress()) + STEERING_OFFSET;
        updateCar(throttle, steering);
    }

    private void updateCar(int throttle, int steering) {
        disposables.add(raspCarService.drive(throttle, steering)
                                      .subscribeOn(io())
                                      .observeOn(mainThread())
                                      .subscribeWith(new DriveDisposableObserver()));
    }

    private void resetCar() {
        throttleSeekBar.setProgress(IDLE);
        steeringSeekBar.setProgress(IDLE);
        calculateChange();
    }

    private class DriveDisposableObserver extends DisposableObserver<ApiDrive> {

        @Override public void onNext(ApiDrive apiDrive) {
            responseTextView.setText("Throttle: " + apiDrive.getThrottle() + " Steering: " + apiDrive.getSteering());
        }

        @Override public void onError(Throwable e) {
            responseTextView.setText("Request failed");
        }

        @Override public void onComplete() {

        }
    }
}
