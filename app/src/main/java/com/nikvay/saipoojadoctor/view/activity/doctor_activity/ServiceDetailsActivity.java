package com.nikvay.saipoojadoctor.view.activity.doctor_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nikvay.saipoojadoctor.R;
import com.nikvay.saipoojadoctor.model.ServiceModel;
import com.nikvay.saipoojadoctor.utils.StaticContent;

public class ServiceDetailsActivity extends AppCompatActivity {


    private  TextView textService,textDuration,textCost;
    private ImageView iv_close;
    private String mTitle="Service Details";
    private ServiceModel serviceModel;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_deatils);

        find_All_IDs();
        events();
    }

    private void events() {

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceDetailsActivity.this, NewAddServiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(StaticContent.IntentKey.SERVICE_DETAIL,serviceModel);
                intent.putExtra(StaticContent.IntentKey.ACTIVITY_TYPE, StaticContent.IntentValue.ACTIVITY_SERVICE_DETAILS);
                startActivity(intent);
            }
        });

    }

    private void find_All_IDs() {
        iv_close = findViewById(R.id.iv_close);
        textService = findViewById(R.id.textService);
        textDuration = findViewById(R.id.textDuration);
        textCost = findViewById(R.id.textCost);
        btnEdit = findViewById(R.id.btnEdit);


        Bundle bundle = getIntent().getExtras();

        if(bundle!=null)
        {
            serviceModel= (ServiceModel) bundle.getSerializable(StaticContent.IntentKey.SERVICE_DETAIL);
            mTitle = bundle.getString(StaticContent.IntentKey.ACTIVITY_TYPE);
        }

        if(mTitle.equals(StaticContent.IntentValue.ACTIVITY_SERVICE_DETAILS))
        {
            textService.setText(serviceModel.getS_name());
            textDuration.setText(serviceModel.getService_time());
            textCost.setText(serviceModel.getService_cost());
        }

    }
}
