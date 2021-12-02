package com.monishvm.simplenotes.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.monishvm.simplenotes.R
import com.monishvm.simplenotes.persistence.Note
import com.monishvm.simplenotes.util.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_add.*
import javax.inject.Inject

import com.monishvm.simplenotes.MLTextRecognition
import com.monishvm.simplenotes.ui.MainActivity
import com.monishvm.simplenotes.ui.NoteViewModel
import kotlinx.android.synthetic.main.fragment_edit.*
import java.io.IOException


class AddFragment : DaggerFragment() {

    @Inject
    lateinit var viewmodelProviderFactory: ViewModelProviderFactory

    lateinit var noteViewModel: NoteViewModel

    // Method #1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity?)?.hideFloatingButton()
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    // Method #2
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_save -> {
                findNavController().popBackStack()
            }
            R.id.option_scan -> {
                openGallery()
                }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 1)
    }

    private fun openCamera() {
        val gallery = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(gallery, 2)
    }

    // Method #3
    private fun saveNoteToDatabase() {
        (activity as MainActivity?)?.showFloatingButton()

        if (validations()) {
            Toast.makeText(activity, "Note is saved", Toast.LENGTH_SHORT).show()
            saveNote()
        }
//            Toast.makeText(activity, "Note is Discarded", Toast.LENGTH_SHORT).show()
    }


    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Method #4
    override fun onDestroyView() {
        hideKeyboard(requireActivity())
        super.onDestroyView()
        saveNoteToDatabase()
    }


    // Method #5
    private fun saveNote() {
        val note = Note(0, addTitle.text.toString().trim(), addDescription.text.toString().trim())

        //If title is null set Empty Title
        if (addTitle.text.isNullOrEmpty()) {
            note.title = "Empty Title"

            //Call viewmodel to save the data
            noteViewModel.insert(note)

        } else {
            //Call viewmodel to save the data
            noteViewModel.insert(note)
        }
    }

    // Method #6
    private fun validations(): Boolean {
        return !(addTitle.text.trim().isEmpty()
                && addDescription.text.trim().isEmpty())
    }


    // Method #7
    private fun setupViewModel() {
        noteViewModel =
            ViewModelProvider(this, viewmodelProviderFactory).get(NoteViewModel::class.java)
    }

    private fun onDetectionSuccess(detectedString: String?) {
        var oldDesc = addDescription.text.toString()
        if (addDescription.text.toString() != "")
            oldDesc = addDescription.text.toString() + "\n\n"
        val newDesc =
            "$oldDesc<- Recognised From Image ->\n$detectedString"
        addDescription.setText(newDesc)
    }

    private fun onDetectionFailure() {
        Toast.makeText(requireContext(), "Cannot Detect Text", Toast.LENGTH_SHORT).show()
    }

    private fun detect(src: Bitmap){
        MLTextRecognition.detectImage(src).addOnSuccessListener {
            if (it.stringValue.trim().isNotEmpty())
                onDetectionSuccess(it.stringValue.trim())
            else
                onDetectionFailure()
        }.addOnFailureListener {
            onDetectionFailure()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            val imageUri = data?.data
            val srcBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            detect(srcBitmap)
        }

    }

}