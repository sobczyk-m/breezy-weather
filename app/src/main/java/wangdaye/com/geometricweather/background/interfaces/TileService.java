package wangdaye.com.geometricweather.background.interfaces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;

import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository;
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;

/**
 * Tile service.
 * */

@RequiresApi(api = Build.VERSION_CODES.N)
public class TileService extends android.service.quicksettings.TileService {

    @Override
    public void onTileAdded() {
        refreshTile(this, getQsTile());
    }

    @Override
    public void onTileRemoved() {
        // do nothing.
    }

    @Override
    public void onStartListening () {
        refreshTile(this, getQsTile());
    }

    @Override
    public void onStopListening () {
        refreshTile(this, getQsTile());
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onClick () {
        try {
            Object statusBarManager = getSystemService("statusbar");
            if (statusBarManager != null) {
                statusBarManager
                        .getClass()
                        .getMethod("collapsePanels")
                        .invoke(statusBarManager);
            }
        } catch (Exception ignored) {

        }
        IntentHelper.startMainActivity(this);
    }

    private static void refreshTile(Context context, Tile tile) {
        if (tile == null) {
            return;
        }
        Location location = LocationEntityRepository.INSTANCE.readLocationList().get(0);
        location = Location.copy(location, WeatherEntityRepository.INSTANCE.readWeather(location));
        if (location.getWeather() != null && location.getWeather().getCurrent() != null) {
            if (location.getWeather().getCurrent().getWeatherCode() != null) {
                tile.setIcon(
                        ResourceHelper.getMinimalIcon(
                                ResourcesProviderFactory.getNewInstance(),
                                location.getWeather().getCurrent().getWeatherCode(),
                                location.isDaylight()
                        )
                );
            }
            if (location.getWeather().getCurrent().getTemperature() != null) {
                tile.setLabel(
                        location.getWeather().getCurrent().getTemperature().getTemperature(
                                context,
                                SettingsManager.getInstance(context).getTemperatureUnit())
                );
            }
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }
}