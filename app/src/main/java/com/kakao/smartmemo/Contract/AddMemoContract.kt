package com.kakao.smartmemo.Contract

import com.kakao.smartmemo.Data.MemoData

interface AddMemoContract {
    interface View {

    }
    interface Presenter{
        fun addMemo(memoData: MemoData)
        //메모를 생성할 때 그룹을 설정하는 데 어떤함수를 만들어야 할 지 모르겠어서 남겨둡니다 총총,,,
    }
}