package com.example.lessonsqlitekotlin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lessonsqlitekotlin.databinding.ActivityMainBinding
import com.example.lessonsqlitekotlin.db.MyAdapter
import com.example.lessonsqlitekotlin.db.MyDbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var bindingClass: ActivityMainBinding
    val myDbManager = MyDbManager(this)
    val myAdapter = MyAdapter(ArrayList(), this)
    private var job: Job? = null
    val builder = AlertDialog.Builder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)
        init()
        initSearchView()
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDb()
    }

    override fun onResume() {
        super.onResume()
        myDbManager.openDb()
        fillAdapter("")
    }

    fun onClickNew(view: View) {
        val i = Intent(this, EditActivity::class.java)
        startActivity(i)
    }

    fun init(){
        bindingClass.rcView.layoutManager = LinearLayoutManager(this)
        val swapHelper = getSwapMg()
        swapHelper.attachToRecyclerView(bindingClass.rcView)
        bindingClass.rcView.adapter = myAdapter
    }

    private fun initSearchView(){
        bindingClass.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                fillAdapter(text!!)
                return true
            }
        })
    }

    private fun fillAdapter(text: String){
        job?.cancel()
       job = CoroutineScope(Dispatchers.Main).launch{
            val list = myDbManager.readDbData(text)
            myAdapter.updateAdapter(list)
            if(list.size > 0){bindingClass.tvNoElements.visibility = View.GONE
            }else{
                bindingClass.tvNoElements.visibility = View.VISIBLE
            }
        }

    }


        fun getSwapMg(): ItemTouchHelper{
        return ItemTouchHelper(object:ItemTouchHelper.
        SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    builder.show()
                }


                }
            }
    override fun createSimpleDialog(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        builder.setTitle("Do you really want to delete this item?")
        builder.setPositiveButton("Yes") { dialogInterFace, i ->
            myAdapter.removeItem(viewHolder.adapterPosition, myDbManager)
        }
        builder.setNegativeButton("No") { dialogInterFace, i ->
            finish()
        }
    }
}

