package com.kakao.smartmemo.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemoData(
    var memoId:String = "",var title:String = "", var date:String = "", var content:String= "",
    var groupId:String= "", var placeId: String = "",
    var placeName:String= "", var latitude: String ="", var longitude: String =""
)//memo list item data
    : Parcelable

