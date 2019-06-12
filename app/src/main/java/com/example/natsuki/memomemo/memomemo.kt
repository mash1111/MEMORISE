package com.example.natsuki.memomemo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class memomemo : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var title: String = ""
    var date: Date = Date()
    var time: String = ""
    var detail: String = ""
}