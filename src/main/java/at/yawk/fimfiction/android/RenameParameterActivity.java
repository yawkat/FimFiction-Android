package at.yawk.fimfiction.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.yawk.fimfiction.data.SearchParameters;

/**
 * @author Jonas Konrad (yawkat)
 */
public class RenameParameterActivity extends Fimtivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rename);

        final SearchParameters parameters = getIntent().<ParamReader>getParcelableExtra("parameters").getParameters();
        String name = getIntent().getStringExtra("name");
        helper().<EditText>view(R.id.text).setText(name);
        helper().<EditText>view(R.id.text).setSelection(name.length());
        helper().<Button>view(R.id.set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper().getParameterManager()
                        .setCustomName(parameters, helper().<EditText>view(R.id.text).getText().toString());
                finish();
            }
        });
        helper().<Button>view(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper().getParameterManager().setCustomName(parameters, null);
                finish();
            }
        });
    }
}
