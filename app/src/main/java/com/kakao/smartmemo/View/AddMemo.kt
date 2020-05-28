package com.kakao.smartmemo.View

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.kakao.smartmemo.Contract.AddMemoContract
import com.kakao.smartmemo.Data.MemoData
import com.kakao.smartmemo.Object.GroupObject
import com.kakao.smartmemo.Presenter.AddMemoPresenter
import com.kakao.smartmemo.R
import java.text.SimpleDateFormat
import java.util.*

class AddMemo : AppCompatActivity(), AddMemoContract.View {
    lateinit var presenter: AddMemoPresenter
    private lateinit var memoToolbar: Toolbar
    private lateinit var saveBtn :Button
    private lateinit var titleEdit : EditText
    private lateinit var dateText : TextView
    private lateinit var contentEdit:EditText
    private lateinit var selectGroupBtn : Button
    private lateinit var groupName : TextView
    private lateinit var placeNameText : TextView
    private lateinit var data :MemoData
    private var groupId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memo)
        //Toolbar 달기
        memoToolbar = findViewById<Toolbar>(R.id.addMemoToolbar)
        setSupportActionBar(memoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        memoToolbar.title = resources.getString(R.string.write_memo)

        //presenter 초기화
        presenter = AddMemoPresenter(this)
        saveBtn = findViewById(R.id.save_button)
        titleEdit = findViewById(R.id.memo_title)
        dateText = findViewById<TextView>(R.id.memo_date)
        contentEdit = findViewById(R.id.memo_content)
        selectGroupBtn = findViewById(R.id.select_group)
        groupName = findViewById(R.id.memo_group)
        placeNameText = findViewById(R.id.place_name)


        val date = Date(System.currentTimeMillis())
        val formatDate = SimpleDateFormat("yyyy.MM.dd")
        var today = formatDate.format(date)
        var memoData :MemoData
        dateText.text = today

        if(intent.hasExtra("memoData")){
            data = intent.getParcelableExtra("memoData")
            placeNameText.text = data.placeName
            titleEdit.setText(data.title)
            contentEdit.setText(data.content)
            groupName.text = data.groupName

        }else{
            saveBtn.setOnClickListener {
                if(groupName.text=="[그룹 선택]"||contentEdit.text.toString()=="") {
                    if (groupName.text == "[그룹 선택]") {
                        Toast.makeText(this, "그룹을 선택하세요!", Toast.LENGTH_SHORT).show()
                    } else if(contentEdit.text.toString()=="") {
                        Toast.makeText(this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "그룹을 선택하고, 내용을입력하세요!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    memoData = MemoData(titleEdit.text.toString(),today,contentEdit.text.toString(),groupName.text.toString(),
                        groupId,0,placeNameText.text.toString(),0.0,0.0)
                    presenter.addMemo(memoData)
                    finish()
                }

            }
        }


        selectGroupBtn.setOnClickListener {
            selectGroup()
        }
    }

    // 뒤로가기 버튼 누르면 이전 액티비티로 돌아가는 것을 판단해주는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun selectGroup(){
        var i = 0
        val items:Array<CharSequence> = Array(GroupObject.groupInfo.size) {""}
        var groupIdList = Array(GroupObject.groupInfo.size){""}

        GroupObject.groupInfo.forEach {
            groupIdList[i] = it.key
            items[i] = it.value
            i++
        }

        val listDialog: AlertDialog.Builder = AlertDialog.Builder(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        )

        listDialog.setTitle("그룹 선택")
            .setItems(items, DialogInterface.OnClickListener { _, which ->
                groupName.text = items[which].toString()
                groupId = groupIdList[which]
            })
            .show()
    }
}