package com.fpliu.newton.util.app

import android.app.Application
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.fpliu.newton.util.appContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class Tests {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    //运行单个测试用例，只能自己配置，因为AS认为你是在做单元测试，会自动配置到JUnit的配置下，而不是Android Test下
    //https://stackoverflow.com/questions/51832349/type-android-junit4-not-present-exception
    @Test
    fun tvIsDisplayed() {
        onView(withId(R.id.tv)).check(matches(isDisplayed()))
    }

    @Test
    fun checkAppContextIsNotNull() {
        assert(appContext is Application)
    }
}