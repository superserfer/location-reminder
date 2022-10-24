package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private var reminderTestDTO = ReminderDTO(
        "Test Title",
        "Test Description",
        "Test Location",
        1.2345,
        1.2345
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun emptyData_hiddenLoading() = runBlockingTest {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is`(false))
    }

    @Test
    fun emptyData_displaysLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is`(true))
    }

    @Test
    fun withReminder_resultNotNull() = runBlockingTest {
        fakeDataSource.saveReminder(reminderTestDTO)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.value?.size, not(0))
        assertThat(remindersListViewModel.showNoData.value, `is`(false))
        assertThat(remindersListViewModel.showLoading.value, `is`(false))

    }
}