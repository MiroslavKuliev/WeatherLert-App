package workers;

/*import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weatherlertapp.R;
import com.example.weatherlertapp.WeatherDataFetch;

public class WeatherNotificationWorker extends Worker {

    private Context context;

    public WeatherNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            double latitude = 40.7128;
            double longitude = -74.0060;

            WeatherDataFetch.isWeatherBad(latitude, longitude, new WeatherDataFetch.WeatherCheckCallback() {
                @Override
                public void onChecked(boolean isBad, String weatherStatus, double temperature) {
                    if (isBad) {
                        showWeatherNotification("Bad Weather Alert: " + weatherStatus);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                }
            });

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void showWeatherNotification(String weatherStatus) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (!notificationManager.areNotificationsEnabled()) {

            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WEATHER_ALERT_CHANNEL")
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Weather Alert!")
                .setContentText(weatherStatus)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(100, builder.build());
    }
}*/
