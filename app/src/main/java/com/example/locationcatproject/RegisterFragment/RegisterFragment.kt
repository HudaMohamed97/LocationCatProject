package com.example.locationcatproject.RegisterFragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.catapplication.utilies.Validation
import com.example.locationcatproject.R
import com.example.myapplication.LoginFragment.RegisterViewModel
import com.example.myapplication.Models.RegisterRequestModel
import kotlinx.android.synthetic.main.register_fragment.*
import java.io.ByteArrayOutputStream


class RegisterFragment : Fragment() {
    private lateinit var root: View
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var email: EditText
    private lateinit var passwordEt: EditText
    private lateinit var rePasswordEt: EditText
    private lateinit var name: EditText
    private lateinit var emailText: String
    private lateinit var passwordText: String
    private var photoString: String = ""
    private lateinit var rePasswordText: String
    private lateinit var nameText: String
    private lateinit var FromFragment: String
    private var fileUri: String = ""
    private var matched = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.register_fragment, container, false)
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FromFragment = arguments?.getString("fromFragment").toString()
        email = root.findViewById(R.id.input_email)
        passwordEt = root.findViewById(R.id.input_password)
        rePasswordEt = root.findViewById(R.id.input_RePassword)
        name = root.findViewById(R.id.input_Name)
        setListeners()
    }

    private fun setListeners() {
        imgProfile.setOnClickListener {
            isStoragePermissionGranted()
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            this.startActivityForResult(photoPickerIntent, 1)
        }
        register_back.setOnClickListener {
            findNavController().navigateUp()
        }
        register_login.setOnClickListener {
            checkErrorEnabled()
            hideKeyboard()
            if (registerViewModel.validateDataInfo(
                    emailText
                    , passwordText
                ) && (nameText.isNotEmpty()) && (rePasswordText.isNotEmpty() && matched)
            ) {
                callRegisterRequest()
            }
        }
    }

    private fun callRegisterRequest() {
        if (photoString != "") {
            progressBar.visibility = View.VISIBLE
            val requestModel = RegisterRequestModel(emailText, passwordText, nameText, fileUri)
            registerViewModel.register(requestModel)
            registerViewModel.getData().observe(this, Observer {
                progressBar.visibility = View.GONE
                if (it != null) {
                    if (it.access_token != "") {
                        openAlertDialog("Register Successfully")
                    } else {
                        var error = it.token_type.replace("[", "")
                        error = error.replace("]", "")
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    openAlertDialog("Network Error")
                }
            })
        } else {
            Toast.makeText(activity, "Please Choose Photo,Thanks", Toast.LENGTH_SHORT).show()
        }

    }


    private fun checkErrorEnabled() {
        getUserInputData()
        isPasswordMatched()
        validate()
    }

    private fun isPasswordMatched() {
        matched = passwordText == rePasswordText
    }


    private fun validate() {
        if (!Validation.validate(emailText)) {
            Toast.makeText(activity, "empty Email please fill it", Toast.LENGTH_SHORT).show()
        } else if (!Validation.validateEmail(emailText)) {
            Toast.makeText(
                activity,
                "Invalid Email Format Please enter valid mail",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!Validation.validate(passwordText)) {
            Toast.makeText(activity, "empty password please fill it", Toast.LENGTH_SHORT).show()

        } else if (!Validation.validate(rePasswordText)) {
            Toast.makeText(activity, "please reconfirm your Password", Toast.LENGTH_SHORT).show()

        } else if (!Validation.validate(nameText)) {
            Toast.makeText(activity, "empty name please fill it", Toast.LENGTH_SHORT).show()

        } else if (passwordEt.length() < 6 || rePasswordText.length < 6) {
            Toast.makeText(activity, "password must be at least 6 characters", Toast.LENGTH_SHORT)
                .show()
        } else if (!matched) {
            Toast.makeText(
                activity,
                "password and confirmed Password not matched",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun getUserInputData() {
        emailText = email.text.toString()
        passwordText = passwordEt.text.toString()
        rePasswordText = rePasswordEt.text.toString()
        nameText = name.text.toString()
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm =
                context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                photoString = data?.data.toString()
                val selectedImage = data?.data
                if (selectedImage != null) {
                    fileUri = getPath(selectedImage)
                    imgProfile.setImageURI(selectedImage)

                  /*  val bitmap: Bitmap =
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImage)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val byteArray = outputStream.toByteArray()
                    //Use your Base64 String as you wish
                    val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    photoString = encoded
                    imgProfile.setImageURI(selectedImage)*/
                }

            }

    }

    private fun getPath(uri: Uri): String {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = activity!!.contentResolver.query(uri, projection, null, null, null);
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        cursor?.moveToFirst()
        if (column_index != null) {
            cursor.getString(column_index)
        }

        return column_index?.let { cursor.getString(it) }!!
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show()

            //resume tasks needing this permission
        } else {
            Toast.makeText(activity, "not access", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAlertDialog(massage: String) {
        val ctw =  ContextThemeWrapper(activity, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
        val builder1 = AlertDialog.Builder(ctw)
        builder1.setMessage(massage)
        builder1.setCancelable(true)
        builder1.setPositiveButton(
            "ok"
        ) { dialog, id -> dialog.cancel() }
        val alert = builder1.create()
        alert.show()
    }

}