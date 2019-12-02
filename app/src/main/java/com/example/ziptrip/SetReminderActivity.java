package com.example.ziptrip;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ziptrip.notifications.AlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class SetReminderActivity extends AppCompatActivity {
    EditText reminderDate, reminderTime;
    Button setReminderBtn;

    String TAG = "Reminder Activity";
    Intent reminderIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminder);

        reminderDate = (EditText)findViewById(R.id.setReminderdateEt);
        reminderTime = (EditText)findViewById(R.id.setReminderTimeEt);
        setReminderBtn = (Button)findViewById(R.id.createReminderBtn);
        reminderIntent = getIntent();

        // When the reminder button is clicked
        setReminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reminderDate.getText() != null && reminderTime.getText() != null){
                    // Create notification at this date and time
                    // Testing notification thingy
                    Log.i(TAG, "About to schedule alarm");
                    try {
                        scheduleAlarm();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(SetReminderActivity.this, "Reminder for " + reminderDate.getText().toString() + " at " + reminderTime.getText().toString() + " has been set!", Toast.LENGTH_SHORT).show();

                    reminderDate.setText(null);
                    reminderTime.setText(null);
                }
            }
        });
    }

    private void scheduleAlarm() throws ParseException {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        String alarmMessage = "Trip " + reminderIntent.getStringExtra("tripname") + " is planned for " + reminderDate.getText().toString() + "!";
        alarmIntent.putExtra("data", alarmMessage);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Get current date
        Calendar cal = Calendar.getInstance();

        long afterTwoMinutes = SystemClock.elapsedRealtime() + (convertToMilliseconds(reminderDate.getText().toString(), reminderTime.getText().toString()) - cal.getTimeInMillis());
                // (dest time - current time)
                //1 * 60 * 1000; // set the time here, convert date to long

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle
                    (AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            afterTwoMinutes, pendingIntent);
        else
            alarmManager.setExact
                    (AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            afterTwoMinutes, pendingIntent);
    }

    private long convertToMilliseconds(String date, String time) throws ParseException {
        String dateTime = date + " " + time;
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date reminderDate = format.parse(dateTime);
        return reminderDate.getTime();
    }
}
