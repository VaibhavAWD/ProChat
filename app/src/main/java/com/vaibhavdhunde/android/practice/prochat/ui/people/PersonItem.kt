package com.vaibhavdhunde.android.practice.prochat.ui.people

import android.content.Context
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.data.User
import com.vaibhavdhunde.android.practice.prochat.util.GlideApp
import com.vaibhavdhunde.android.practice.prochat.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class PersonItem(
    val person: User,
    val userId: String,
    private val context: Context
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.item_person_name.text = person.name
        viewHolder.item_person_bio.text = person.bio
        if (person.profilePicturePath != null) {
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilePicturePath))
                .placeholder(R.drawable.ic_default_profile_img)
                .into(viewHolder.item_person_profile_img)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_person
    }
}