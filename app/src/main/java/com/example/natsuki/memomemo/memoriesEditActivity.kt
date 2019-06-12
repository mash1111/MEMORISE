package com.example.natsuki.memomemo
//データベースへ予定を登録するアクティビティを作成

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.media.Image
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_memories_edit.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.yesButton
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.ImageView
import com.example.natsuki.memomemo.R.layout.activity_memories_edit


class memoriesEditActivity : AppCompatActivity()
        , SimpleAlertDialog.OnClickListener
        , DatePickerFragment.OnDateSelectedListener
        , TimePickerFragment.OnTimeSelectedListener {

    override fun onSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        dateText.text = android.text.format.DateFormat.format("yyyy/MM/dd", c)
    }

    override fun onSelected(hourOfDay: Int, minute: Int) {
        timeText.text = "%1$02d:%2$02d".format(hourOfDay, minute)
    }

    override fun onPositiveClick() {
        finish()
    }

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("onReceive", false) == true) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                else ->
                    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
            val dialog = SimpleAlertDialog()
            dialog.show(supportFragmentManager, "alert_dialog")
        }

        setContentView(R.layout.activity_memories_edit);


        realm = Realm.getDefaultInstance()

        val scheduleId = intent?.getLongExtra("schedule_id", -1L)
        if (scheduleId != -1L) {
            val schedule = realm.where<memomemo>().equalTo("id", scheduleId).findFirst()
            dateText.setText(DateFormat.format("yyyy/MM/dd", schedule?.date))
            timeText.setText(schedule?.time)
            titleEdit.setText(schedule?.title)
            detailEdit.setText(schedule?.detail)
            delete.visibility = View.VISIBLE
        } else {
            delete.visibility = View.INVISIBLE
        }

        save.setOnClickListener {
            when (scheduleId) {
                -1L -> {
                    realm.executeTransaction {
                        val maxId = realm.where<memomemo>().max("id")
                        val nextId = (maxId?.toLong() ?: 0L) + 1
                        val memomemo = realm.createObject<memomemo>(nextId)
                        dateText.text.toString().toDate("yyyy/MM/dd")?.let {
                            memomemo.date = it
                        }
                        memomemo.title = titleEdit.text.toString()
                        memomemo.time = timeText.text.toString()
                        memomemo.detail = detailEdit.text.toString()

                        val date = "${dateText.text} ${timeText.text}".toDate()
                        when {
                            date != null -> {
                                val calendar = Calendar.getInstance()
                                calendar.time = date
                                calendar.add(Calendar.MINUTE, -30)
                                setAlarmManager(calendar)
                            }
                        }
                    }
                    alert("保存しました！") {
                        yesButton { finish() }
                    }.show()
                }
                else -> {
                    realm.executeTransaction {
                        val schedule = realm.where<memomemo>().equalTo("id", scheduleId).findFirst()
                        dateText.text.toString().toDate("yyyy/MM/dd")?.let { schedule?.date = it }
                        schedule?.title = titleEdit.text.toString()
                        schedule?.time = timeText.text.toString()
                        schedule?.detail = detailEdit.text.toString()
                    }

                    val date = "${dateText.text} ${timeText.text}".toDate()
                    when {
                        date != null -> {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            calendar.add(Calendar.MINUTE, -30)
                            setAlarmManager(calendar)
                        }
                    }
                    alert("修正しました") {
                        yesButton { finish() }
                    }.show()
                }
            }
        }



        delete.setOnClickListener {
            realm.executeTransaction {
                realm.where<memomemo>().equalTo("id", scheduleId)?.findFirst()?.deleteFromRealm()
            }
            alert("削除しました") {
                yesButton { finish() }
            }.show()
        }

        dateText.setOnClickListener {
            val dialog = DatePickerFragment()
            dialog.show(supportFragmentManager, "date_dialog")
        }

        timeText.setOnClickListener {
            val dialog = TimePickerFragment()
            dialog.show(supportFragmentManager, "time_dialog")
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAlarmManager(calendar: Calendar) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis, null
                )
                am.setAlarmClock(info, pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis, pending
                )
            }
            else -> {
                am.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis, pending
                )
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm"): Date? {
        val sdFormat = try {
            SimpleDateFormat(pattern)
        } catch (e: IllegalArgumentException) {
            null
        }
        val date = sdFormat?.let {
            try {
                it.parse(this)
            } catch (e: ParseException) {
                null
            }
        }
        return date
    }
}