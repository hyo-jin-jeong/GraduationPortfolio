package com.kakao.smartmemo.View

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.kakao.smartmemo.R
import com.kakao.smartmemo.Contract.MemberChangeContract
import com.kakao.smartmemo.Presenter.MemberChangePresenter
import kotlinx.android.synthetic.main.member_change_view.*

class MemberDataChange :AppCompatActivity(),MemberChangeContract.View{
    lateinit var presenter : MemberChangeContract.Presenter

    lateinit var memberToolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_change_view)
        presenter = MemberChangePresenter(this)

        memberToolbar = findViewById(R.id.member_toolbar)
        setSupportActionBar(memberToolbar)
        member_icon.setClipToOutline(true)

        //앱 이름 없애는-
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        text7.setOnClickListener {
            var listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                var hour = 0
                var am_pm = "오전"
                var m = minute.toString()
                if (hourOfDay == 0) {
                    am_pm = "오전"
                    hour = 12
                }
                if (hourOfDay >= 12) {
                    am_pm = "오후"
                    hour = hourOfDay % 12
                    if (hour == 0) {
                        hour = 12
                    }

                }
                else{
                    hour = hourOfDay
                }
                if (minute == 0) {
                    m = "00"
                }
                time_text.text = "${am_pm} ${hour} : ${m} "
            }
            val dialog = TimePickerDialog(this,listener,12,0,false)

            dialog.show()
        }
        button.setOnClickListener{
            presenter.updateUser()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
            }
        }

}