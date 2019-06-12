package com.example.natsuki.memomemo

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.example.natsuki.memomemo.R.id.dateText
import com.example.natsuki.memomemo.R.id.save
import io.realm.Realm
import io.realm.kotlin.where

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("onReceive", false) == true) {
            val dialog = SimpleAlertDialog()
            dialog.show(supportFragmentManager, "alert_dialog")
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->
            //画面遷移
            startActivity<memoriesEditActivity>()
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        realm = Realm.getDefaultInstance()
        val memories = realm.where<memomemo>().findAll()
        listView.adapter = memoAdapter(memories)

        listView.setOnItemClickListener { parent, view, position, id ->
            val schedule = parent.getItemAtPosition(position) as memomemo
            startActivity<memoriesEditActivity>("schedule_id" to schedule.id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}






