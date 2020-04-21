package com.kakao.smartmemo.Presenter

import com.kakao.smartmemo.Contract.ModifyGroupContract
import com.kakao.smartmemo.Model.GroupModel


class ModifyGroupPresenter : ModifyGroupContract.Presenter{
    
    private  var groupModel : GroupModel
    private var view : ModifyGroupContract.View


    constructor(view: ModifyGroupContract.View){
        this.view = view
        this.groupModel = GroupModel()
    }


    override fun updateGroup() {
        //GroupObject에 데이터 세팅후, UserObject의 group_info수정
        groupModel.updateGroup()
    }
}