package com.nikvay.saipoojadoctor.view.activity.doctor_activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nikvay.saipoojadoctor.R;
import com.nikvay.saipoojadoctor.apicallcommon.ApiClient;
import com.nikvay.saipoojadoctor.apicallcommon.ApiInterface;
import com.nikvay.saipoojadoctor.model.AppointmentListModel;
import com.nikvay.saipoojadoctor.model.DoctorModel;
import com.nikvay.saipoojadoctor.model.SuccessModel;
import com.nikvay.saipoojadoctor.utils.ErrorMessageDialog;
import com.nikvay.saipoojadoctor.utils.NetworkUtils;
import com.nikvay.saipoojadoctor.utils.SharedUtils;
import com.nikvay.saipoojadoctor.utils.ShowProgress;
import com.nikvay.saipoojadoctor.utils.StaticContent;
import com.nikvay.saipoojadoctor.view.adapter.doctor_adapter.AppointmentListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentListActivity extends AppCompatActivity {

    private String label, appointmentName, doctor_id, TAG = getClass().getSimpleName(), user_id,date="";
    private ImageView iv_close, iv_no_data_found, iv_date;
    private TextView textAppointmentTitleName;
    private RecyclerView recyclerViewAppointmentList;
    private ErrorMessageDialog errorMessageDialog;
    private ArrayList<AppointmentListModel> appointmentListModelArrayList = new ArrayList<>();
    private AppointmentListAdapter appointmentListAdapter;
    private ApiInterface apiInterface;
    private ShowProgress showProgress;
    private EditText edt_search_appointment;
    private ArrayList<DoctorModel> doctorModelArrayList = new ArrayList<>();
    private SwipeRefreshLayout refreshAppointmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);

        find_All_IDs();
        events();


        if (NetworkUtils.isNetworkAvailable(AppointmentListActivity.this))
            appointmentListCall();
        else
            NetworkUtils.isNetworkNotAvailable(AppointmentListActivity.this);

    }


    private void events() {
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edt_search_appointment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                appointmentListAdapter.getFilter().filter(edt_search_appointment.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        iv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AppointmentListActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        CharSequence strDate = null;
                        Time chosenDate = new Time();
                        chosenDate.set(day, month, year);

                        long dateAttendance = chosenDate.toMillis(true);
                        strDate = DateFormat.format("yyyy-MM-dd", dateAttendance);

                        date = (String) strDate;

                        if (NetworkUtils.isNetworkAvailable(AppointmentListActivity.this))
                            appointmentListCall();
                        else
                            NetworkUtils.isNetworkNotAvailable(AppointmentListActivity.this);

                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });
        refreshAppointmentList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isNetworkAvailable(AppointmentListActivity.this))
                    appointmentListCall();
                else
                    NetworkUtils.isNetworkNotAvailable(AppointmentListActivity.this);
                refreshAppointmentList.setRefreshing(false);
            }
        });


    }

    private void find_All_IDs() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        iv_close = findViewById(R.id.iv_close);
        edt_search_appointment = findViewById(R.id.edt_search_appointment);
        iv_date = findViewById(R.id.iv_date);
        refreshAppointmentList = findViewById(R.id.refreshAppointmentList);
        textAppointmentTitleName = findViewById(R.id.textAppointmentTitleName);
        recyclerViewAppointmentList = findViewById(R.id.recyclerViewAppointmentList);
        iv_no_data_found = findViewById(R.id.iv_no_data_found);
        showProgress = new ShowProgress(AppointmentListActivity.this);
        errorMessageDialog = new ErrorMessageDialog(AppointmentListActivity.this);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            label = bundle.getString(StaticContent.IntentKey.STATUS);
            appointmentName = bundle.getString(StaticContent.IntentKey.APPOINTMENT);
            textAppointmentTitleName.setText(appointmentName);

        }

        if(appointmentName.equalsIgnoreCase("Today's Appointments"))
        {
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }

        doctorModelArrayList = SharedUtils.getUserDetails(AppointmentListActivity.this);
        doctor_id = doctorModelArrayList.get(0).getDoctor_id();
        user_id = doctorModelArrayList.get(0).getUser_id();

        recyclerViewAppointmentList.setHasFixedSize(true);
        recyclerViewAppointmentList.setLayoutManager(new LinearLayoutManager(this));

    }

    private void appointmentListCall() {
        showProgress.showDialog();
        Call<SuccessModel> call = apiInterface.appointmentList(doctor_id, label, user_id,date);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                showProgress.dismissDialog();
                String str_response = new Gson().toJson(response.body());
                Log.e("" + TAG, "Response >>>>" + str_response);


                try {
                    if (response.isSuccessful()) {
                        SuccessModel successModel = response.body();
                        String message = null, code = null;
                        if (successModel != null) {
                            message = successModel.getMsg();
                            code = successModel.getError_code();
                            appointmentListModelArrayList.clear();
                            if (code.equalsIgnoreCase("1")) {

                                appointmentListModelArrayList = successModel.getAppoinmentListModelArrayList();

                                if (appointmentListModelArrayList.size() != 0) {
                                    Collections.reverse(appointmentListModelArrayList);
                                    appointmentListAdapter = new AppointmentListAdapter(AppointmentListActivity.this, appointmentListModelArrayList);
                                    recyclerViewAppointmentList.setAdapter(appointmentListAdapter);
                                    iv_no_data_found.setVisibility(View.GONE);
                                    appointmentListAdapter.notifyDataSetChanged();

                                } else {
                                    iv_no_data_found.setVisibility(View.VISIBLE);
                                    appointmentListAdapter.notifyDataSetChanged();
                                }

                            } else {
                                errorMessageDialog.showDialog("Response Not Working");
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                showProgress.dismissDialog();
                errorMessageDialog.showDialog(t.getMessage());
            }
        });
    }

}
