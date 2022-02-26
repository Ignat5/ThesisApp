package com.mycompany.thesis1.mvvm.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.ui.IconGenerator
import com.mycompany.thesis1.DrawerLocker
import com.mycompany.thesis1.R
import com.mycompany.thesis1.adapters.UsersMapListAdapter
import com.mycompany.thesis1.dialogs.SetGroupDialog
import com.mycompany.thesis1.models.UserMap
import com.mycompany.thesis1.mvvm.model.User
import com.mycompany.thesis1.mvvm.model.UsersResource
import com.mycompany.thesis1.services.MyLocationService
import com.mycompany.thesis1.utils.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.layout_drawer_header.*
import kotlinx.android.synthetic.main.layout_users_map_bottom_sheet.*
import kotlinx.coroutines.flow.collect
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class MainFragment : Fragment(R.layout.fragment_main), OnMapReadyCallback {

    private val viewModel by viewModels<MainViewModel>()
    private var googleMap: GoogleMap? = null
    private var listOfMarkers: MutableList<UserMap> = mutableListOf()
    private var sheetBehavior: BottomSheetBehavior<LinearLayoutCompat>? = null
    private var usersAdapter: UsersMapListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        initStateListeners()
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) mapViewBundle =
            savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        checkPermissions()
        initRecyclerView()
        hideKeyboard()

    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady...")
        googleMap = p0
        googleMap?.uiSettings?.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
        }
        initDataListeners()
        initUIListeners()
    }

    private fun initUIListeners() {
        vCurrentGroup.setOnClickListener {
            viewModel.currentGroup.value?.let { currentGroup ->
                val dialog = SetGroupDialog(
                    listOfGroups = viewModel.getGroups(),
                    currentGroupId = currentGroup.groupId,
                    listener = object : SetGroupDialog.Listener {
                        override fun onChoose(chosenGroupId: String) {
                            Log.d(TAG, "onChoose: group with id $chosenGroupId was chosen")
                            viewModel.onCurrentGroupChanged(chosenGroupId)
                        }
                    }
                )
                dialog.isCancelable = true
                dialog.show(requireActivity().supportFragmentManager, "TAG_GROUP_DIALOG")
            }
        }
        ivMenu.setOnClickListener {
            drawer_layout.open()
        }
        nav_view.setNavigationItemSelectedListener {
            drawer_layout.close()
            when (it.itemId) {
                R.id.menuItemGroup -> {
                    Log.d("myTag", "openDrawer: groups clicked")
                    val action = MainFragmentDirections.actionMainFragmentToGroupsFragment()
                    findNavController().navigate(action)
                }
                R.id.menuItemRequests -> {
                    val action = MainFragmentDirections.actionMainFragmentToRequestsHostFragment()
                    findNavController().navigate(action)
                }
            }
            false
        }
        llLogout.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.signOut()
            val action = MainFragmentDirections.actionMainFragmentToAuthFragment2()
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvUsers.layoutManager = linearLayoutManager
        //rvUsers.setHasFixedSize(true)
        usersAdapter = UsersMapListAdapter(object : UsersMapListAdapter.Listener {
            override fun onClicked(userId: String) {
                Log.d("myTag", "onClicked: userId: $userId")
                val userMarker = listOfMarkers.find { marker -> marker.id == userId }
                userMarker?.marker?.position?.let { position ->
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(position.latitude, position.longitude))
                        .zoom(15f)
                        .build()
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            cameraPosition
                        ), 1000, null
                    )
                }
            }
        })
        rvUsers.adapter = usersAdapter
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                ivBottomSheetArrow.rotation = slideOffset * 180
            }
        })
        iToolbar.setOnClickListener {
            sheetBehavior?.let { sheet ->
                if (sheet.state != BottomSheetBehavior.STATE_EXPANDED)
                    sheet.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    sheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
//        ivBottomSheetArrow.setOnClickListener {
//            sheetBehavior?.let { sheet->
//                if(sheet.state != BottomSheetBehavior.STATE_EXPANDED)
//                    sheet.state = BottomSheetBehavior.STATE_EXPANDED
//                else
//                    sheet.state = BottomSheetBehavior.STATE_COLLAPSED
//            }
//        }
    }

    private fun initDataListeners() {
        viewModel.currentGroup.observe(viewLifecycleOwner) { currentGroup ->
            currentGroup?.let {
                tvCurrentGroup.text = currentGroup.groupName
                Log.d(TAG, "fragment: tvGroupName: ${currentGroup.groupName}")
            }
        }
        viewModel.usersResource.observe(viewLifecycleOwner) { resource ->
            if (googleMap != null)
                when (resource) {
                    is UsersResource.SuccessAllUsers -> {
                        Log.d(TAG, "observe: SuccessAllUsers")
                        resource.data?.let { users ->
                            googleMap?.clear()
                            listOfMarkers.clear()
                            onGetUsers(users)
                            usersAdapter?.submitList(users)
                        }
                        resource.currentGroup?.let {
                            Log.d(TAG, "initDataListeners: set current group name: ${it.groupName}")
                            //tvCurrentGroup.text = it.groupName
                        }
                    }
                    is UsersResource.SuccessNewUser -> {
                        Log.d(TAG, "observe: SuccessNewUser")
                        if (listOfMarkers.isEmpty())
                            resource.data?.let { users ->
                                onGetUsers(users)
                            }
                        else
                            resource.updatedUsers?.let { users ->
                                onGetUsers(users)
                            }
                        usersAdapter?.submitList(resource.data)
                    }
                    is UsersResource.SuccessUpdateUser -> {
                        Log.d(TAG, "observe: SuccessUpdateUser")
                        if (listOfMarkers.isEmpty())
                            resource.data?.let { users ->
                                onGetUsers(users)
                            }
                        else
                            resource.updatedUsers?.let { updatedUsers ->
                                onUpdateUsers(updatedUsers)
                            }
                        usersAdapter?.submitList(resource.data)
                    }
                    is UsersResource.SuccessDeleteUser -> {
                        Log.d(TAG, "observe: SuccessDeleteUser")
                        if (listOfMarkers.isEmpty())
                            resource.data?.let { users ->
                                onGetUsers(users)
                            }
                        else
                            resource.updatedUsers?.let { users ->
                                onDeleteUsers(users)
                            }
                        usersAdapter?.submitList(resource.data)
                    }
                    is UsersResource.Error -> {
                        Log.d(TAG, "observer: Error! Message: ${resource.message}")
                    }
                    is UsersResource.Loading -> {

                    }
                }
        }
    }

    private fun onGetUsers(users: List<User>) {
        var profileUser: User? = null
        for (user in users) {
            if (googleMap != null && user.latitude >= 0 && user.longitude >= 0) {
                val markerOptions = MarkerOptions()
                val location = LatLng(user.latitude, user.longitude)
                markerOptions.position(location)
                val iconGenerator = IconGenerator(context)
                val icon = iconGenerator.makeIcon(user.userName)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("some title")
                    .snippet("some snippet")
                val newMarker = googleMap?.addMarker(markerOptions)
                listOfMarkers.add(
                    UserMap(
                        id = user.userId,
                        marker = newMarker!!
                    )
                )
                if (user.userId == viewModel.profileId) profileUser = user
            } else {
                Log.d(TAG, "onGetAllUsers: user ${user.userName} can't be shown on the map")
            }
        }
        Log.d(TAG, "onGetUsers: listOfMarkers.size: ${listOfMarkers.size}")
        if (profileUser != null && viewModel.profileId != null) {
            CURRENT_USER_NAME = profileUser!!.userName
            drawer_layout.apply {
                tvUserName.text = profileUser.userName
                tvUserEmail.text = profileUser.userId
            }
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(profileUser.latitude, profileUser.longitude))
                .zoom(15f)
                .build()
            googleMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition
                ), 500, null
            )
            viewModel.isMyFirstUpdate = false
        }

    }

    private fun onUpdateUsers(updatedUsers: List<User>) {
        var profileUser: User? = null
        for (user in updatedUsers) {
            if (user.latitude >= 0 && user.longitude >= 0) {
                if (user.userId == viewModel.profileId) profileUser = user
                val updatedMarker = listOfMarkers.find { it.id == user.userId }
                if (updatedMarker != null) {
                    updatedMarker.marker.position = LatLng(user.latitude, user.longitude)
                } else {
                    if (googleMap != null) {
                        Log.d(TAG, "onUpdateUsers: updatedMarker == null! Need to create one")
                        val markerOptions = MarkerOptions()
                        val location = LatLng(user.latitude, user.longitude)
                        markerOptions.position(location)
                        val iconGenerator = IconGenerator(context)
                        val icon = iconGenerator.makeIcon(user.userName)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .title("some title")
                            .snippet("some snippet")
                        val newMarker = googleMap?.addMarker(markerOptions)
                        listOfMarkers.add(
                            UserMap(
                                id = user.userId,
                                marker = newMarker!!
                            )
                        )
                    }
                }
            } else {
                Log.d(TAG, "onUpdateUsers: user ${user.userName} can't be shown on the map")
            }
        }
        if (viewModel.isMyFirstUpdate && profileUser != null && viewModel.profileId != null) {
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(profileUser.latitude, profileUser.longitude))
                .zoom(15f)
                .build()
            googleMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition
                ), 1000, null
            )
            viewModel.isMyFirstUpdate = false
        }
    }

    private fun onDeleteUsers(deletedUsers: List<User>) {
        for (user in deletedUsers) {
            val removedMarker = listOfMarkers.find { it.id == user.userId }
            removedMarker?.marker?.remove()
        }
    }

//    private fun setMarker() {
//        val markerOptions = MarkerOptions()
//        val location = LatLng(50.0,50.0)
//        markerOptions.position(location)
//        val iconGenerator = IconGenerator(context)
//        val icon = iconGenerator.makeIcon("Someone")
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("some title").snippet("some snippet")
//        val newMarker = googleMap?.addMarker(markerOptions)
//        val cameraPosition = CameraPosition.Builder()
//            .target(LatLng(location.latitude, location.longitude))
//            .zoom(15f)
//            .build()
//        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),1000,null)
//    }

    private fun initStateListeners() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.mainEventFlow.collect { event ->
                when (event) {
                    is MainViewModel.MainEvents.CheckPermissions -> {
                        checkPermissions()
                    }
                    is MainViewModel.MainEvents.ShowLoading -> {
                        //pbMain.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun startLocationService() {
        val intent =
            Intent(activity?.applicationContext, MyLocationService::class.java).apply {
                action = Constants.ACTION_START_SERVICE
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.applicationContext?.startForegroundService(intent)
        } else {
            activity?.applicationContext?.startService(intent)
        }
    }


    private fun checkPermissions() {
        Log.d(TAG, "checkPermissions...")
        val locationPermissionsState = beforeRequestPermissions(
            REQUEST_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        when (locationPermissionsState) {
            BeforeRequestPermissionResult.AlreadyGranted -> {
                Log.d(TAG, "checkPermissions: permission is already granted")
                getBackgroundPermission()
//                startLocationService()
            }
            BeforeRequestPermissionResult.ShowRationale -> {
                beforeRequestPermissions(
                    REQUEST_LOCATION, true,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            BeforeRequestPermissionResult.Requested -> Unit
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult...")
        when (requestCode) {
            REQUEST_LOCATION -> {
                when (afterRequestPermissions(permissions, grantResults)) {
                    AfterRequestPermissionsResult.Granted -> {
                        Log.d(TAG, "REQUEST_LOCATION: permission granted")
                        getBackgroundPermission()
//                        startLocationService()
                    }
                    AfterRequestPermissionsResult.Denied -> {
                        Log.d(TAG, "REQUEST_LOCATION: permission denied")
                        showMessage("Для полноценной работы приложения необходимо разрешение на отслеживание вашего местоположения")
                    }
                    AfterRequestPermissionsResult.NeverAskAgain -> {
                        Log.d(TAG, "REQUEST_LOCATION: permission NeverAskAgain")
                        showMessage("Для полноценной работы приложения необходимо разрешение на отслеживание вашего местоположения")
                    }
                }
            }
            REQUEST_BACKGROUND_LOCATION -> {
                when (afterRequestPermissions(permissions, grantResults)) {
                    AfterRequestPermissionsResult.Granted -> {
                        Log.d(TAG, "REQUEST_BACKGROUND_LOCATION: permission granted")
                        startLocationService()
                    }
                    AfterRequestPermissionsResult.Denied -> {
                        Log.d(TAG, "REQUEST_BACKGROUND_LOCATION: permission denied")
                        showMessage("Для лучшего качества работы приложения разрешите отслеживание местоположения всегда: Настройки -> Приложения -> ThesisApp1 -> Местоположение -> Разрешить в любом режиме")
                        startLocationService()
                    }
                    AfterRequestPermissionsResult.NeverAskAgain -> {
                        Log.d(TAG, "REQUEST_BACKGROUND_LOCATION: permission NeverAskAgain")
                        showMessage("Для лучшего качества работы приложения разрешите отслеживание местоположения всегда: Настройки -> Приложения -> ThesisApp1 -> Местоположение -> Разрешить в любом режиме")
                        startLocationService()
                    }
                }
            }
            else -> {
                Log.d(TAG, "onRequestPermissionsResult-else: ")
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun getBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(
                TAG,
                "getBackgroundPermission: Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q - TRUE"
            )
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "getBackgroundPermission: ACCESS_FINE_LOCATION - GRANTED")
                val locationPermissionsState = beforeRequestPermissions(
                    REQUEST_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                when (locationPermissionsState) {
                    BeforeRequestPermissionResult.AlreadyGranted -> {
                        startLocationService()
                        Log.d(
                            TAG,
                            "ACCESS_BACKGROUND_LOCATION: checkPermissions: permission is already granted"
                        )
                    }
                    BeforeRequestPermissionResult.ShowRationale -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Улучшение качества")
                            .setMessage("Для лучшего качества работы приложения разрешите отслеживание местоположения в любом режиме")
                            .setPositiveButton("Ok") { dialog, _ ->
                                dialog.dismiss()
                                beforeRequestPermissions(
                                    REQUEST_BACKGROUND_LOCATION, true,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                )
                            }
                            .show()
                        Log.d(
                            TAG,
                            "ACCESS_BACKGROUND_LOCATION: checkPermissions: ShowRationale"
                        )
                    }
                    BeforeRequestPermissionResult.Requested -> Unit
                }
            }
        } else
            startLocationService()
    }

    private fun showMessage(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Предупреждение")
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) mapView.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listOfMarkers.clear()
        Log.d(TAG, "onDestroyView: ")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity?.currentFocus
        if (view == null)
            view = View(requireActivity())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        private val MAPVIEW_BUNDLE_KEY = "MAPVIEW_BUNDLE_KEY"
        private const val TAG = "myTag"
        private const val REQUEST_LOCATION = 101
        private const val REQUEST_BACKGROUND_LOCATION = 102
        var CURRENT_USER_NAME = ""
    }
}