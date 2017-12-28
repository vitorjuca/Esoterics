package com.br.esoterics.dev.home

import com.br.esoterics.dev.Center
import com.br.esoterics.dev.helpers.dataThrowable
import com.google.firebase.database.*

/**
 * Created by vitor_juca on 28/12/17.
 */
class HomeRepository {

    private val firebase by lazy { initFireBase() }

    fun requestAllCenters(onSuccess: (it: ArrayList<Center>) -> Unit, onError: (t: Throwable) -> Unit){
        val request = firebase.child("Centers")
        request.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                onError.invoke(dataThrowable)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    var listOfCenters: ArrayList<Center> = arrayListOf()
                    dataSnapshot.children.forEach{
                        var obj = it.getValue(Center::class.java)
                        if (obj != null) {
                            listOfCenters.add(obj)
                        }
                    }
                    onSuccess.invoke(listOfCenters)
                }
            }

        })
    }


    private fun initFireBase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }
}