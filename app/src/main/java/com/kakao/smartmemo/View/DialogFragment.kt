package com.kakao.smartmemo.View

import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.kakao.smartmemo.Adapter.DialogSectionsPagerAdapter
import com.kakao.smartmemo.Contract.DialogContract
import com.kakao.smartmemo.Data.MemoData
import com.kakao.smartmemo.Data.PlaceAlarmData
import com.kakao.smartmemo.Data.PlaceData
import com.kakao.smartmemo.Presenter.DialogPresenter
import com.kakao.smartmemo.R

class DialogFragment : DialogFragment(), DialogContract.View {
    private lateinit var presenter : DialogContract.Presenter
    private lateinit var adapter: DialogSectionsPagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var myDialog: Dialog
    private lateinit var myToolbar: Toolbar
    private var type: Int? = null

    private var memo = mutableListOf<PlaceData>()
    private var todo = mutableListOf<PlaceData>()

    private var memoDataList = mutableListOf<MemoData>()
    private var todoDataList = mutableListOf<PlaceAlarmData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        myDialog = super.onCreateDialog(savedInstanceState)
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myDialog.setContentView(R.layout.main_dialog)
        var params: WindowManager.LayoutParams = myDialog.window!!.attributes
        var display = activity?.windowManager?.defaultDisplay
        var point = Point()
        display?.getSize(point)
        params.width = point.x - 100
        params.height = point.y - 200
        myDialog.window!!.attributes = params

        return myDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.main_dialog, container, false)
        val indicator = view.findViewById<CircleIndicator>(
            R.id.circle_indicator
        )
        myToolbar = view.findViewById<Toolbar>(R.id.toolbar)
        myToolbar.setNavigationIcon(R.drawable.back)
        myToolbar.setNavigationOnClickListener {
            dismiss()
        }

        // Set up the ViewPager with the sections adapter.
        viewPager = view.findViewById<ViewPager>(R.id.dialog_pager)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) { }
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) { }
            override fun onPageSelected(p0: Int) {
                if(type == 2)
                    indicator.selectDot(p0)
            }
        })

        if (type == 2)
            indicator.createDotPanel(2,
                R.drawable.indicator_dot_on,
                R.drawable.indicator_dot_off, 0)

        return view
    }

    fun setAdapter() {
        adapter = DialogSectionsPagerAdapter(memoDataList, todoDataList,
            childFragmentManager
        )
        adapter.setCurType(type!!)

        viewPager.adapter = adapter
        presenter.setDialogAdapterModel(adapter)
        presenter.setDialogAdapterView(adapter)
    }

    override fun onSuccessMemo(memoList: MutableList<MemoData>){
        memoDataList = memoList

        setAdapter()
    }

    override fun onSuccessTodo(todoList: MutableList<PlaceAlarmData>){
        todoDataList = todoList

        setAdapter()
    }

    fun setPresenter() {
        presenter =  DialogPresenter(this)
    }
    fun setCurType(type: Int) {
        this.type = type
    }

    fun setMemoList(memo: MutableList<PlaceData>, latitude: Double, longitude: Double) {
        var j = 0

        for (i in memo) {
            if(i.latitude == latitude && i.longitude == longitude) {
                this.memo.add(i)
            }
            if(memo.size-1 == j){
                presenter.getMemoList(this.memo)
            }
            j++
        }
    }

    fun setTodoList(todoList: MutableList<PlaceData>, latitude: Double, longitude: Double) {
        for ((j, i) in todoList.withIndex()) {
            if(i.latitude == latitude && i.longitude == longitude) {
                this.todo.add(i)
            }
            if(todoList.size-1 == j){
                presenter.getTodoList(todo)
            }
        }
    }
}