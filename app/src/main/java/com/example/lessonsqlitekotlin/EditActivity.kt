package com.example.lessonsqlitekotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.lessonsqlitekotlin.databinding.EditActivityBinding
import com.example.lessonsqlitekotlin.db.MyDbManager
import com.example.lessonsqlitekotlin.db.MyIntentConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    lateinit var bindingClass: EditActivityBinding
    var id = 0
    var isEditState = false
    val imageRequestCode = 10
    var tempImageUri = "empty"
    val myDbManager = MyDbManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = EditActivityBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)
        getMyIntents()
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDb()
    }

    override fun onResume() {
        super.onResume()
        myDbManager.openDb()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == imageRequestCode){
            bindingClass.imMainImage.setImageURI(data?.data)
            tempImageUri = data?.data.toString()
            contentResolver.takePersistableUriPermission(data?.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun onClickAddImage(view: View) {
        bindingClass.mainImageLayout.visibility = View.VISIBLE
        bindingClass.fbAddImage.visibility = View.GONE
    }

    fun onClickDeleteImage(view: View) {
        bindingClass.mainImageLayout.visibility = View.GONE
        bindingClass.fbAddImage.visibility = View.VISIBLE
        tempImageUri = "empty"
    }

    fun onClickChooseImage(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, imageRequestCode)
    }

    fun onClickSave(view: View) {
        val myTitle = bindingClass.edTitle.text.toString()
        val myDesc = bindingClass.edDesc.text.toString()
        if(myTitle != "" && myDesc != ""){
            CoroutineScope(Dispatchers.Main).launch {
                if(isEditState){
                    myDbManager.updateItem(myTitle, myDesc, tempImageUri, id, getCurrentTime())
                } else {
                    myDbManager.insertToDb(myTitle, myDesc, tempImageUri, getCurrentTime())
                }
                finish()
            }
        }
    }

    fun onEditEnable(view: View){
        bindingClass.edTitle.isEnabled = true
        bindingClass.edDesc.isEnabled = true
        bindingClass.fbEdit.visibility = View.GONE
        bindingClass.fbAddImage.visibility = View.VISIBLE
        if(tempImageUri == "empty")return
        bindingClass.imButtonEditImage.visibility = View.VISIBLE
        bindingClass.imButtonDeleteImage.visibility = View.VISIBLE
    }

    fun getMyIntents(){
        bindingClass.fbEdit.visibility = View.GONE
        val i = intent
        if(i != null){
            if(i.getStringExtra(MyIntentConstants.I_TITLE_KEY) != null){
                bindingClass.fbAddImage.visibility = View.GONE
                bindingClass.edTitle.setText(i.getStringExtra(MyIntentConstants.I_TITLE_KEY))
                isEditState = true
                bindingClass.edTitle.isEnabled = false
                bindingClass.edDesc.isEnabled = false
                bindingClass.fbEdit.visibility = View.VISIBLE
                bindingClass.edDesc.setText(i.getStringExtra(MyIntentConstants.I_DESC_KEY))
                id = i.getIntExtra(MyIntentConstants.I_ID_KEY, 0)
                if(i.getStringExtra(MyIntentConstants.I_URI_KEY) != "empty" ){
                    bindingClass.mainImageLayout.visibility = View.VISIBLE
                    tempImageUri = i.getStringExtra(MyIntentConstants.I_URI_KEY)!!
                    bindingClass.imMainImage.setImageURI(Uri.parse(tempImageUri))
                    bindingClass.imButtonDeleteImage.visibility = View.GONE
                    bindingClass.imButtonEditImage.visibility = View.GONE
                    }
            }
        }
    }

    private fun getCurrentTime():String{
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yy kk:mm", Locale.getDefault())
        return formatter.format(time)
    }
}
