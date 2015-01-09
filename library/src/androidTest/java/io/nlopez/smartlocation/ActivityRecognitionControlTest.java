package io.nlopez.smartlocation;

import android.content.Context;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.util.MockActivityRecognitionProvider;
import io.nlopez.smartlocation.utils.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
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
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = SmartLocation.with(
                context).activityRecognition();
        activityRecognitionControl.provider(mockProvider);

        verify(mockProvider).init(eq(context), any(Logger.class));
    }

    @Test
    public void test_activity_recognition_control_start_defaults() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();

        activityRecognitionControl.start(activityUpdatedListener);
        verify(mockProvider).start(activityUpdatedListener, DEFAULT_PARAMS);
    }

    @Test
    public void test_location_control_get_last_location() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();
        activityRecognitionControl.getLastActivity();

        verify(mockProvider).getLastActivity();
    }

    @Test
    public void test_location_control_stop() {
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = createActivityRecognitionControl();
        activityRecognitionControl.stop();

        verify(mockProvider).stop();
    }

    private SmartLocation.ActivityRecognitionControl createActivityRecognitionControl() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        SmartLocation.ActivityRecognitionControl activityRecognitionControl = SmartLocation.with(
                context).activityRecognition();
        activityRecognitionControl.provider(mockProvider);
        return activityRecognitionControl;
    }

}
