package com.mobi.ass10_sqlite

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab10sqlite.StudentsAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_delete_layout.view.*
import kotlinx.android.synthetic.main.insert_layout.view.*
import kotlinx.android.synthetic.main.insert_layout.view.edt_age
import kotlinx.android.synthetic.main.insert_layout.view.edt_id
import kotlinx.android.synthetic.main.insert_layout.view.edt_name

class MainActivity : AppCompatActivity() {
    var dbHandler: DatabaseHelper? = null
    var studentlist = arrayListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DatabaseHelper(this)
        dbHandler?.getWritableDatabase()
        callStudentData()
        recycler_view.adapter = StudentsAdapter(studentlist, applicationContext)
        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.addItemDecoration(DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL))

        recycler_view.addOnItemTouchListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                editDeleteDialog(position)
            }
        })
    }
    fun callStudentData(){
        studentlist.clear()
        studentlist.addAll(dbHandler!!.getAllStudents())
        recycler_view.adapter?.notifyDataSetChanged()
    }

    fun addStudent(v : View){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.insert_layout, null)
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setView(mDialogView)
        val mAlertDialog = mBuilder.show()

        mDialogView.btnAdd.setOnClickListener{
            var id = mDialogView.edt_id.text.toString()
            var name = mDialogView.edt_name.text.toString()
            var age = mDialogView.edt_age.text.toString().toInt()

            var result = dbHandler?.insertStudent(Student(id = id, name = name, age = age))
            if(result!! > -1){
                Toast.makeText(applicationContext, "Insert Successfully", Toast.LENGTH_SHORT).show()
                callStudentData()
                mAlertDialog.dismiss()
            }else{
                Toast.makeText(applicationContext, "Insert Error", Toast.LENGTH_SHORT).show()
            }
        }

        mDialogView.btnReset.setOnClickListener {
            mDialogView.edt_age.text.clear()
            mDialogView.edt_id.text.clear()
            mDialogView.edt_name.text.clear()
        }
    }

    fun editDeleteDialog(position: Int){
        val std = studentlist[position]

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.edit_delete_layout, null)
        mDialogView.edt_id.isEnabled = false
        mDialogView.edt_id.setText(std.id)
        mDialogView.edt_name.setText(std.name)
        mDialogView.edt_age.setText(std.age.toString())

        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setView(mDialogView)
        val mAlertDialog = mBuilder.show()

        mDialogView.btnUpdate.setOnClickListener {
            var id = mDialogView.edt_id.text.toString()
            var name = mDialogView.edt_name.text.toString()
            var age = mDialogView.edt_age.text.toString().toInt()
            var result = dbHandler?.updateStudent(Student(id = id, name = name, age = age))

            if(result!! > -1){
                Toast.makeText(applicationContext, "Update Successfully", Toast.LENGTH_SHORT).show()
                callStudentData()
            }else{
                Toast.makeText(applicationContext, "Update Error", Toast.LENGTH_SHORT).show()
            }
            mAlertDialog.dismiss()
        }

        mDialogView.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val positiveBtn = { dialog: DialogInterface, Which: Int ->
                var result = dbHandler?.deleteStudent(std.id)
                if (result!! > -1) {
                    Toast.makeText(applicationContext, "Delete Successfully", Toast.LENGTH_SHORT).show()
                    callStudentData()
                }else{
                    Toast.makeText(applicationContext, "Delete Error", Toast.LENGTH_SHORT).show()
                }
                mAlertDialog.dismiss()
            }
            val negativeBtn = { dialog: DialogInterface, Which: Int ->
                Toast.makeText(applicationContext, "Cancel", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }

            builder.setTitle("Warning")
            builder.setMessage("Do You Want to Delete this Student Information ?")
            builder.setPositiveButton("No", negativeBtn)
            builder.setNegativeButton("Yes", positiveBtn)
            builder.show()
        }
    }
}

interface OnItemClickListener{
    fun onItemClicked(position: Int, view: View)
}

fun RecyclerView.addOnItemTouchListener(onClickListener: OnItemClickListener){
    this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener{
        override fun onChildViewAttachedToWindow(view: View) {
            view?.setOnClickListener{
                val holder = getChildViewHolder(view)
                onClickListener.onItemClicked(holder.adapterPosition, view)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
            view?.setOnClickListener(null)
        }

    })
}
