package io.github.nagromc86.nextstep;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** The app sends its upcoming flights, stays and activities; the widget shows the next one. */
@CapacitorPlugin(name = "NextStep")
public class NextStepPlugin extends Plugin {
    @PluginMethod
    public void setSteps(PluginCall call) {
        NextStepWidget.setSteps(getContext(), call.getString("list", "[]"));
        call.resolve();
    }
}
