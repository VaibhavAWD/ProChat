package com.vaibhavdhunde.android.practice.prochat.ui.people

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ListenerRegistration

import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.ui.chat.ChatActivity
import com.vaibhavdhunde.android.practice.prochat.util.FirestoreUtil
import com.vaibhavdhunde.android.practice.prochat.util.USERNAME
import com.vaibhavdhunde.android.practice.prochat.util.USER_ID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_people.*
import org.jetbrains.anko.support.v4.startActivity

class PeopleFragment : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private lateinit var peopleSection: Section

    private var shouldInitListPeople = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)

        userListenerRegistration = FirestoreUtil.addUserListenerRegistration(requireContext(),
            this::updateListPeople)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitListPeople = true
    }

    private fun updateListPeople(users: List<Item>) {
        fun init() {
            list_people.apply {
                adapter = GroupAdapter<ViewHolder>().apply {
                    peopleSection = Section(users)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitListPeople = false
        }

        fun updateItems() {
            peopleSection.update(users)
        }

        if (shouldInitListPeople) {
            init()
        } else {
            updateItems()
        }
    }

    private val onItemClick = OnItemClickListener { item, view ->
        val personItem = item as PersonItem
        startActivity<ChatActivity>(
            USERNAME to personItem.person.name,
            USER_ID to personItem.userId
        )
    }

}
