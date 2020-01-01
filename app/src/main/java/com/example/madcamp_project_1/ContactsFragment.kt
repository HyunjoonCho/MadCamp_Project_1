package com.example.madcamp_project_1

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList

private val Context.inputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

class ListViewItem : Comparable<ListViewItem> {
    lateinit var picture: String
    lateinit var name: String
    lateinit var phone_number: String

    override fun compareTo(other: ListViewItem): Int {
        return this.name.compareTo(other.name)
    }
}

class ListViewAdapter : BaseAdapter(), Filterable {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private var listViewItemList = ArrayList<ListViewItem>()
    private var filteredItemList = listViewItemList


    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    override fun getCount(): Int {
        return filteredItemList.size
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val context = parent.context

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.contactview_item, parent, false)
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        val iconImageView = view!!.findViewById(R.id.pictureView) as ImageView
        val titleTextView = view.findViewById(R.id.nameView) as TextView

        // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
        val listViewItem = filteredItemList[position]

        // 아이템 내 각 위젯에 데이터 반영
        if(listViewItem.picture != "-1") {
            val uri = Uri.parse(listViewItem.picture)
            iconImageView.setImageURI(uri)
        } else {
            iconImageView.setImageResource(R.mipmap.profile)
        }
        titleTextView.setText(listViewItem.name)

        return view
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun sortByName(){
        Collections.sort(filteredItemList)
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return filteredItemList[position]
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    fun addItem(picture: String, name: String, phone_number: String) {
        val item = ListViewItem()

        item.picture = picture
        item.name = name
        item.phone_number = phone_number

        listViewItemList.add(item)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                if(constraint == null || constraint.length == 0){
                    results.values = listViewItemList
                    results.count = listViewItemList.size
                } else {
                    var itemList = ArrayList<ListViewItem>()

                    for(item in listViewItemList){
                        if(item.name.toLowerCase().contains(constraint.toString().toLowerCase())){
                            itemList.add(item)
                        }
                    }

                    results.values = itemList
                    results.count = itemList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItemList = results?.values as ArrayList<ListViewItem>

                if(results?.count!! > 0) {
                    notifyDataSetChanged()
                } else{
                    notifyDataSetInvalidated()
                }
            }
        }
    }

}

class ContactsFragment : Fragment() {
    companion object {
        val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            }
            else {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),PERMISSIONS_REQUEST_READ_CONTACTS)
                //callback onRequestPermissionsResult
            }
        } else {
            loadContacts()
        }
    }

    private fun loadContacts(){
        val l_view = view?.findViewById(R.id.listContacts) as ListView
        val adapter= ListViewAdapter()
        getContacts(adapter)
        adapter.sortByName()
        l_view.adapter = adapter
        l_view.onItemClickListener = AdapterView.OnItemClickListener{parent, v, position, id ->
            val item = parent.getItemAtPosition(position) as ListViewItem

            //val profile = item.picture
            val uri = item.picture
            val name = item.name
            val phone_number = item.phone_number

            val builder = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.contact_dialog, null)

            val dialogPic = dialogView.findViewById<ImageView>(R.id.profile_pic)
            val dialogName = dialogView.findViewById<TextView>(R.id.name)
            val dialogNum = dialogView.findViewById<TextView>(R.id.phone_number)
            val call = dialogView.findViewById<ImageButton>(R.id.dial)
            val text = dialogView.findViewById<ImageButton>(R.id.sms)

            if(uri != "-1") { dialogPic.setImageURI(Uri.parse(uri))  }
            dialogName.setText(name)
            dialogNum.setText(phone_number)

            call.setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone_number)))
                startActivity(dialIntent)
            }

            text.setOnClickListener{
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + Uri.encode(phone_number)))
                startActivity(smsIntent)
            }

            builder.setView(dialogView)
            val dialog = builder.create()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

            dialog.show()
         }
        val editTextFilter = view?.findViewById(R.id.editText) as EditText


        editTextFilter.setOnKeyListener(object: View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    requireContext().inputMethodManager.hideSoftInputFromWindow(editTextFilter.windowToken, 0)

                    return true
                }
                return false
            }
        })

        editTextFilter.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus) {
                    requireContext().inputMethodManager.hideSoftInputFromWindow(editTextFilter.windowToken, 0)
                }
            }
        }


        editTextFilter.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val filterText = s.toString()
                (l_view.adapter as ListViewAdapter).filter.filter(filterText)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun getContacts(adapter: ListViewAdapter){
        val resolver: ContentResolver = context!!.contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
            null)

        if (cursor!=null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                var photo_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = context!!.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone != null && cursorPhone.count >= 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(
                                cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            if(photo_uri==null){ photo_uri="-1" }
                            adapter.addItem(photo_uri, name, phoneNumValue)

                        }
                    }
                    cursorPhone?.close()
                }
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor?.close()
    }

}// Required empty public constructor