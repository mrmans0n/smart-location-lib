package io.nlopez.smartlocation.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import io.nlopez.smartlocation.utils.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LocationPermissionsManager}
 */
@RunWith(RobolectricTestRunner.class)
public class LocationPermissionsManagerTest {
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private Activity mActivity;
    @Mock private Context mContext;
    @Mock private Logger mLogger;
    @Mock private LocationPermissionsManager.ActivityCompatProxy mActivityCompatProxy;

    private LocationPermissionsManager mManager;

    @Before
    public void setup() {
        mManager = new LocationPermissionsManager(mLogger, mActivityCompatProxy);
    }

    @Test
    public void testAllPermissionsEnabled() {
        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        assertThat(mManager.permissionsEnabled(mContext)).isTrue();
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mActivity)).isTrue();
    }

    @Test
    public void testAllPermissionsDisabled() {
        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_DENIED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_DENIED);
        assertThat(mManager.permissionsEnabled(mContext)).isFalse();
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mActivity)).isFalse();
    }

    @Test
    public void testOnlyOnePermissionEnabled() {
        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_DENIED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        assertThat(mManager.permissionsEnabled(mContext)).isFalse();
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mActivity)).isFalse();


        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_DENIED);
        assertThat(mManager.permissionsEnabled(mContext)).isFalse();
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mActivity)).isFalse();
    }

    @Test
    public void testRequestPermissionsDoesntWorkForNonActivities() {
        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_DENIED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_DENIED);
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mContext)).isFalse();
    }

    @Test
    public void testRequestPermissionsRequestedForBothPermissions() {
        setPermissionValue(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_DENIED);
        setPermissionValue(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_DENIED);
        assertThat(mManager.permissionsEnabledOrRequestPermissions(mActivity)).isFalse();
        final String[] permissionArray = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        verify(mActivityCompatProxy).requestPermissions(eq(mActivity), eq(permissionArray), eq(LocationPermissionsManager.PERMISSIONS_REQUEST_CODE));
    }

    private void setPermissionValue(@NonNull String permission, int value) {
        when(mActivityCompatProxy.checkSelfPermission(mActivity, permission)).thenReturn(value);
        when(mActivityCompatProxy.checkSelfPermission(mContext, permission)).thenReturn(value);
    }
}