package com.example.madcamp_project_1

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.*
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList

class ListViewItem : Comparable<ListViewItem> {
    lateinit var picture: Drawable
    lateinit var name: String
    lateinit var phone_number: String

    override fun compareTo(other: ListViewItem): Int {
        return this.name.compareTo(other.name)
    }
}

class ListViewAdapter : BaseAdapter() {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private var listViewItemList = ArrayList<ListViewItem>()

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    override fun getCount(): Int {
        return listViewItemList.size
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

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        val listViewItem = listViewItemList[position]

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.picture)
        titleTextView.setText(listViewItem.name)

        return view
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun sortByName(){
        Collections.sort(listViewItemList)
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return listViewItemList[position]
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    fun addItem(picture: Drawable, name: String, phone_number: String) {
        val item = ListViewItem()

        item.picture = picture
        item.name = name
        item.phone_number = phone_number

        listViewItemList.add(item)
    }
}

class FirstFragment : Fragment() {
    companion object {
        val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
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
            val name = item.name
            val phone_number = item.phone_number

            val builder = AlertDialog.Builder(requireContext())

            builder.setTitle(name)
            builder.setMessage(phone_number)
            builder.setNeutralButton("Dial"){_, _ ->
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone_number)))
                startActivity(dialIntent)
            }
            builder.setPositiveButton("Text"){_, _ ->
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + Uri.encode(phone_number)))
                startActivity(smsIntent)
            }

            val alertDialog = builder.create()

            alertDialog.show()

         }
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
                            Log.e("Name ===>",phoneNumValue);
                            adapter.addItem(ContextCompat.getDrawable(requireContext(), R.drawable.profile_user)!!, name, phoneNumValue)
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