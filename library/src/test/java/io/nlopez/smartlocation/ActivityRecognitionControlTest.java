package io.nlopez.smartlocation;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;

import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.rx.ObservableFactory;
import io.nlopez.smartlocation.util.MockActivityRecognitionProvider;
import io.nlopez.smartlocation.utils.Logger;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityRecognitionControlTest {

    private static final ActivityParams DEFAULT_PARAMS = ActivityParams.NORMAL;

    private MockActivityRecognitionProvider mockProvider;
    private OnActivityUpdatedListener activityUpdatedListener;

    @Before
    public void setup() {
        mockProvider = mock(MockActivityRecognitionProvider.class);
        activityUpdatedListener = mock(OnActivityUpdatedListener.class);
    }

    @Test
    public void test_activity_recognition_control_init() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SmartLocation smartLocation = new SmartLocation.Builder(context).preInitialize(false).build();
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = smartLocation.activity(mockProvider);

        verifyZeroInteractions(mockProvider);

        smartLocation = new SmartLocation.Builder(context).build();
        activityRecognitionControl = smartLocation.activity(mockProvider);
        verify(mockProvider).init(eq(context), any(Logger.class));
    }

    @Test
    public void test_activity_recognition_control_start_defaults() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();

        activityRecognitionControl.start(activityUpdatedListener);
        verify(mockProvider).start(activityUpdatedListener, DEFAULT_PARAMS);
    }

    @Test
    public void test_activity_recognition_get_last_location() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();
        activityRecognitionControl.getLastActivity();

        verify(mockProvider).getLastActivity();
    }

    @Test
    public void test_activity_recognition_stop() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();
        activityRecognitionControl.stop();

        verify(mockProvider).stop();
    }

    @Test
    public void test_observable_activity() {
        TestSubscriber<DetectedActivity> testSubscriber = new TestSubscriber<>();
        MockActivityRecognitionProvider provider = new MockActivityRecognitionProvider();
        Observable<DetectedActivity> activityObservable = ObservableFactory.from(
                SmartLocation.with(RuntimeEnvironment.application.getApplicationContext())
                        .activity(provider)
        );
        activityObservable.subscribe(testSubscriber);

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.UNKNOWN,100);
        provider.fakeEmitActivity(detectedActivity);
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(detectedActivity));
    }

    private SmartLocation.ActivityRecognitionControl createActivityRecognitionControl() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SmartLocation smartLocation = new SmartLocation.Builder(context).preInitialize(false).build();
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = smartLocation.activity(mockProvider);
        return activityRecognitionControl;
    }

}
