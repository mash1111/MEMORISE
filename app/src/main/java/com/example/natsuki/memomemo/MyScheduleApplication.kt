package com.example.natsuki.memomemo

import android.app.Application
import io.realm.Realm

//Applicationクラスを継承したクラス作成を行い、データのApplication内共有を行う
class MyScheduleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}