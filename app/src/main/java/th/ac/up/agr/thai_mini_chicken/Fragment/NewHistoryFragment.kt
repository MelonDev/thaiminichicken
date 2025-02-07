package th.ac.up.agr.thai_mini_chicken.Fragment


import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_new_history.view.*
import th.ac.up.agr.thai_mini_chicken.Adapter.NewHistoryAdapter
import th.ac.up.agr.thai_mini_chicken.Data.CardData
import th.ac.up.agr.thai_mini_chicken.Firebase.Firebase

import th.ac.up.agr.thai_mini_chicken.R
import th.ac.up.agr.thai_mini_chicken.Tools.Date
import th.ac.up.agr.thai_mini_chicken.Tools.QuickRecyclerView
import java.util.*

class NewHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: NewHistoryAdapter

    val ID = FirebaseAuth.getInstance().currentUser!!.uid

    private var process: Boolean = false

    private var arrData = ArrayList<CardData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_history, container, false)

        recyclerView = QuickRecyclerView(context!!
                , view.new_history_recycler_view
                , "spacial"
                , 1
                , "vertical"
                , true
                , "alway"
                , "high")
                .recyclerView()

        adapter = NewHistoryAdapter(this, ID, arrData)
        recyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()

        onPost()
    }

    fun onPost(){
        val databaseReference = Firebase.reference.child("ผู้ใช้").child(ID).child("รายการ").child("ใช้งาน")

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                arrData.clear()
                recyclerView.adapter!!.notifyDataSetChanged()
                view?.new_history_empty_area?.visibility = View.VISIBLE
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null) {
                    if (!process) {
                        process = true

                        onDataLoad()
                    }
                } else {
                    arrData.clear()
                    recyclerView.adapter!!.notifyDataSetChanged()
                    view?.new_history_empty_area?.visibility = View.VISIBLE
                }
            }
        })

    }

    fun onDataLoad(){
        val databaseReference = Firebase.reference.child("ผู้ใช้").child(ID).child("รายการ").child("ใช้งาน")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e("","")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null) {
                    arrData.clear()


                    var count = 0
                    val size = p0.children.count()

                    for(it in p0.children){
                        val key = it.key.toString()

                        val rf = Firebase.reference.child("ผู้ใช้").child(ID).child("รายการ").child("ใช้งาน").child(key).child("รายละเอียด")
                        rf.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Log.e("","")
                            }

                            override fun onDataChange(p1: DataSnapshot) {
                                if (p1.value != null) {
                                    val slot = p1.getValue(CardData::class.java)!!

                                    if(slot.status.contentEquals("INACTIVE")) {
                                        if (arrData.size == 0) {
                                            val calendar = Calendar.getInstance()
                                            val x = Date().reDate(slot.createDate)
                                            calendar.time = x
                                            val a = CardData()
                                            a.apply {
                                                cardID = "null"
                                                createDate = slot.createDate
                                            }
                                            arrData.add(slot)
                                            arrData.add(a)
                                            recyclerView.adapter!!.notifyDataSetChanged()
                                            check()
                                        } else {
                                            val today = Calendar.getInstance()

                                            val last = Calendar.getInstance()
                                            val calendar = Calendar.getInstance()

                                            val a = Calendar.getInstance()
                                            val b = Calendar.getInstance()

                                            val lSlot = arrData[arrData.lastIndex]

                                            val x = Date().reDate(slot.createDate)
                                            val y = Date().reDate(lSlot.createDate)

                                            a.time = x
                                            b.time = y

                                            calendar.apply {
                                                set(Calendar.YEAR, a.get(Calendar.YEAR))
                                                set(Calendar.MONTH, a.get(Calendar.MONTH))
                                                set(Calendar.DAY_OF_MONTH, a.get(Calendar.DAY_OF_MONTH))
                                            }

                                            last.apply {
                                                set(Calendar.YEAR, b.get(Calendar.YEAR))
                                                set(Calendar.MONTH, b.get(Calendar.MONTH))
                                                set(Calendar.DAY_OF_MONTH, b.get(Calendar.DAY_OF_MONTH))
                                            }


                                            val difference = calendar.timeInMillis - last.timeInMillis
                                            val days = (difference / (1000 * 60 * 60 * 24)).toInt()

                                            if (days == 0) {
                                                val a = arrData[arrData.lastIndex]
                                                arrData.removeAt(arrData.lastIndex)
                                                arrData.add(slot)
                                                arrData.add(a)
                                            } else {
                                                val a = CardData()
                                                a.apply {
                                                    cardID = "null"
                                                    createDate = slot.createDate
                                                }
                                                arrData.add(slot)
                                                arrData.add(a)

                                            }
                                            recyclerView.adapter!!.notifyDataSetChanged()
                                            check()
                                        }

                                    }
                                }
                            }
                        })

                        count+=1

                        if(count == size){
                            process = false

                            recyclerView.adapter!!.notifyDataSetChanged()
                            check()
                        }

                    }




                }
            }
        })
    }

    fun check(){
        if(arrData.isEmpty()){
            view?.new_history_empty_area?.visibility = View.VISIBLE
        }else{
            view?.new_history_empty_area?.visibility = View.GONE

        }
    }


}
