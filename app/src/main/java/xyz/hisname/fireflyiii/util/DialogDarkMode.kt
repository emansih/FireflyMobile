package xyz.hisname.fireflyiii.util

import android.app.DatePickerDialog
import android.content.Context
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import java.util.*

class DialogDarkMode {

    fun showCorrectDatePickerDialog(context: Context, dateListener: DatePickerDialog.OnDateSetListener,
                                    calendar: Calendar){
        if(AppPref(PreferenceManager.getDefaultSharedPreferences(context)).nightModeEnabled){
            DatePickerDialog(context,  R.style.AppTheme_Dark_Dialog_DatePicker,
                    dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
        } else {
            DatePickerDialog(context, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}