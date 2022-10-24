package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private var reminderDataItemValid = ReminderDataItem(
        title = "Test Title",
        description = "Test Description",
        location = "Test Location",
        longitude = 1.2345,
        latitude = 1.2345
    )

    private var reminderDataItemInvalid = ReminderDataItem(
        title = null,
        description = null,
        location = null,
        longitude = null,
        latitude = null
    )

    private var pointOfInterest = PointOfInterest(LatLng(1.2345, 1.2345), "Test", "Test")

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun onClear() {
        saveReminderViewModel.reminderTitle.value = reminderDataItemValid.title
        saveReminderViewModel.reminderDescription.value = reminderDataItemValid.description
        saveReminderViewModel.reminderSelectedLocationStr.value = reminderDataItemValid.location
        saveReminderViewModel.latitude.value = reminderDataItemValid.latitude
        saveReminderViewModel.longitude.value = reminderDataItemValid.longitude
        saveReminderViewModel.selectedPOI.value = pointOfInterest

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value, nullValue())
        assertThat(saveReminderViewModel.reminderDescription.value, nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, nullValue())
        assertThat(saveReminderViewModel.latitude.value, nullValue())
        assertThat(saveReminderViewModel.longitude.value, nullValue())
        assertThat(saveReminderViewModel.selectedPOI.value, nullValue())
    }

    @Test
    fun saveReminder() = runBlocking {
        saveReminderViewModel.saveReminder(reminderDataItemValid)
        val result = fakeDataSource.getReminder(reminderDataItemValid.id)
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertThat(result.data, notNullValue())
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun validateEnteredData_Valid() {
        val result = saveReminderViewModel.validateEnteredData(reminderDataItemValid)
        assertThat(result, `is`(true))
    }

    @Test
    fun validateEnteredData_Invalid() {
        val result = saveReminderViewModel.validateEnteredData(reminderDataItemInvalid)
        assertThat(result, `is`(false))
    }

}