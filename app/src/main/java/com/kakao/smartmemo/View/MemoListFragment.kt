package com.kakao.smartmemo.View

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.smartmemo.Adapter.MemoListAdapter
import com.kakao.smartmemo.Adapter.MemoListDeleteAdapter
import com.kakao.smartmemo.Contract.MemoContract
import com.kakao.smartmemo.Data.MemoData
import com.kakao.smartmemo.Presenter.MemoPresenter
import com.kakao.smartmemo.R
import kotlinx.android.synthetic.main.activity_main.*

class MemoListFragment : Fragment(), MemoContract.View {
    private lateinit var presenter: MemoPresenter
    private lateinit var recyclerView1 : RecyclerView
    private lateinit var memoAdapter: MemoListAdapter
    private lateinit var memoDeleteAdapter: MemoListDeleteAdapter
    private lateinit var bottomnavigationview : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.memo_list_fragment, container, false)
        presenter = MemoPresenter(this)
        recyclerView1 = view.findViewById(R.id.rv_memo_list!!)as RecyclerView
        bottomnavigationview = view.findViewById(R.id.navigationview_bottom)
        memoAdapter =  MemoListAdapter()
        memoDeleteAdapter = MemoListDeleteAdapter()

        recyclerView1.adapter = memoAdapter

        presenter.setMemoAdapterModel(memoAdapter)
        presenter.setMemoAdapterView(memoAdapter)
        recyclerView1.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)



        //하단 메뉴
        bottomnavigationview.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.removeItem -> {
                    true
                }
                R.id.cancelItem -> {
                    bottomnavigationview.visibility = View.GONE //하단메뉴 안보이게
                    recyclerView1.adapter = memoAdapter
                    true
                }
            }
            true
        }

        bottomnavigationview.visibility = View.GONE; //하단메뉴 안보이게
        return view
    }
    override fun showMemoItem(memoData: MemoData) {

        var intent = Intent(view?.context, ShowMemo::class.java)
        intent.putExtra("todo_id",memoData.title)
        startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        (activity as MainActivity).toolbar.title="Memo List"

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.select_group_in_list, menu)
        //menu?.getItem(1)?.setIcon(context?.let { ContextCompat.getDrawable(it, R.drawable.camera) })
        //menu.add("여행").setIcon(R.drawable.group_color) 메모 동적 생성시 참고
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item?.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.select_group -> {
                selectGroup()
                return true
            }
            R.id.delete_memo ->{
                deleteTodo()
                return true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun selectGroup(){
        val items = arrayOf<CharSequence>("메모", "TODO 장소알람")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(
            this.context,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        )
        listDialog.setTitle("그룹 선택")
            .setItems(items, DialogInterface.OnClickListener { _, which ->
                //그룹선택 구현
            })
            .show()
    }

    private fun deleteTodo() {
        recyclerView1.adapter = memoDeleteAdapter
        presenter.setMemoDeleteAdapterModel(memoDeleteAdapter)
        presenter.setMemoDeleteAdapterView(memoDeleteAdapter)
        bottomnavigationview.visibility = View.VISIBLE
    }



}