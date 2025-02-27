package com.dragon.ide.ui.dialogs.blockeditor;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import com.dragon.ide.R;
import com.dragon.ide.databinding.LayoutAddTextInBlockBinding;
import com.dragon.ide.listeners.ValueListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddTextInBlockDialog extends MaterialAlertDialogBuilder {
  private Activity activity;
  private LayoutAddTextInBlockBinding binding;
  private boolean containError = false;
  private String errorMessage = "";

  public AddTextInBlockDialog(Activity activity, ValueListener listener) {
    super(activity);
    this.activity = activity;
    binding = LayoutAddTextInBlockBinding.inflate(activity.getLayoutInflater());
    setTitle(R.string.enter_value);
    setView(binding.getRoot());
    if (binding.valueEditor.getText().toString().length() == 0) {
      errorMessage = activity.getString(R.string.value_should_not_be_empty);
      containError = true;
    } else {
      errorMessage = "";
      containError = false;
    }
    binding.TextInputLayout1.setError(errorMessage);
    binding.TextInputLayout1.setErrorEnabled(containError);
    binding.valueEditor.addTextChangedListener(
        new TextWatcher() {

          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

          @Override
          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (binding.valueEditor.getText().toString().length() == 0) {
              errorMessage = "";
              containError = false;
              binding.TextInputLayout1.setErrorEnabled(true);
            } else {
              errorMessage = "";
              containError = false;
            }
            binding.TextInputLayout1.setError(errorMessage);
            binding.TextInputLayout1.setErrorEnabled(containError);
          }

          @Override
          public void afterTextChanged(Editable arg0) {}
        });
    setPositiveButton(
        R.string.done,
        (param1, param2) -> {
          if (containError) {
            listener.onError(errorMessage);
          } else {
            listener.onSubmitted(binding.valueEditor.getText().toString());
          }
        });
    setNegativeButton(R.string.cancel, (param1, param2) -> {});
  }
}
