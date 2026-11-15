package io.github.nagromc86.nextstep;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** 4 x 1 widget: the next flight, stay or activity of the trip — when, where, and one tap to the map. */
public class NextStepWidget extends AppWidgetProvider {

    static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("next_step", Context.MODE_PRIVATE);
    }

    static void setSteps(Context c, String json) {
        prefs(c).edit().putString("list", json == null ? "[]" : json).apply();
        try {
            AppWidgetManager mgr = AppWidgetManager.getInstance(c);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(c, NextStepWidget.class));
            if (ids.length > 0) {
                draw(c, mgr, ids);
            }
        } catch (Exception ignored) {
        }
    }

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);

    private static long parse(String s) {
        if (TextUtils.isEmpty(s)) return 0;
        try {
            if (s.length() == 10) s = s + "T00:00";
            Date d = ISO.parse(s.substring(0, 16));
            return d == null ? 0 : d.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String rel(long t, boolean allDay) {
        long now = System.currentTimeMillis();
        Calendar a = Calendar.getInstance(), b = Calendar.getInstance();
        b.setTimeInMillis(t);
        int d = b.get(Calendar.DAY_OF_YEAR) - a.get(Calendar.DAY_OF_YEAR) + 365 * (b.get(Calendar.YEAR) - a.get(Calendar.YEAR));
        String hm = new SimpleDateFormat("HH:mm", Locale.FRANCE).format(new Date(t));
        String day = d == 0 ? "Aujourd'hui" : d == 1 ? "Demain" : new SimpleDateFormat("EEE d MMM", Locale.FRANCE).format(new Date(t));
        if (allDay) return day;
        String s = day + " " + hm;
        long min = (t - now) / 60000L;
        if (min > 0 && min < 24 * 60) {
            s += min < 60 ? " · dans " + min + " min" : " · dans " + (min / 60) + " h" + (min % 60 >= 5 ? " " + String.format(Locale.FRANCE, "%02d", min % 60) : "");
        }
        return s;
    }

    static void draw(Context c, AppWidgetManager mgr, int[] ids) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.ns_widget);
        JSONObject next = null;
        boolean ongoing = false;
        long now = System.currentTimeMillis();
        try {
            JSONArray a = new JSONArray(prefs(c).getString("list", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o == null) continue;
                long s = parse(o.optString("start")), e = parse(o.optString("end"));
                if (e <= s) e = s + (o.optBoolean("allDay") ? 24 * 3600000L : 3600000L);
                if (e < now) continue;
                if (next == null || s < parse(next.optString("start"))) {
                    next = o;
                    ongoing = s <= now;
                }
            }
        } catch (Exception ignored) {
        }
        Intent open = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        PendingIntent openPi = open == null ? null : PendingIntent.getActivity(c, 1, open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (next == null) {
            v.setTextViewText(R.id.ns_kind, "CARNET DE VOYAGE");
            v.setTextViewText(R.id.ns_title, "Aucune étape à venir");
            v.setTextViewText(R.id.ns_when, "Ajoute tes vols, logements et activités");
            v.setViewVisibility(R.id.ns_where, View.GONE);
            v.setViewVisibility(R.id.ns_map, View.GONE);
            v.setTextViewText(R.id.ns_icon, "✈");
        } else {
            String kind = next.optString("kind");
            v.setTextViewText(R.id.ns_icon, "flight".equals(next.optString("type")) ? "✈" : "stay".equals(next.optString("type")) ? "🏨" : "📍");
            v.setTextViewText(R.id.ns_kind, (ongoing ? "EN COURS · " : "PROCHAINE ÉTAPE · ") + kind.toUpperCase(Locale.FRANCE));
            v.setTextViewText(R.id.ns_title, next.optString("title"));
            long s = parse(next.optString("start"));
            String w = ongoing && !TextUtils.isEmpty(next.optString("end")) && next.optString("end").length() > 10
                    ? "Jusqu'à " + next.optString("end").substring(11, 16)
                    : rel(s, next.optBoolean("allDay"));
            v.setTextViewText(R.id.ns_when, w);
            String where = next.optString("where");
            v.setTextViewText(R.id.ns_where, where);
            v.setViewVisibility(R.id.ns_where, TextUtils.isEmpty(where) ? View.GONE : View.VISIBLE);
            String maps = next.optString("maps");
            if (!TextUtils.isEmpty(maps)) {
                Intent m = new Intent(Intent.ACTION_VIEW, Uri.parse(maps)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.setOnClickPendingIntent(R.id.ns_map, PendingIntent.getActivity(c, 2, m, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                v.setViewVisibility(R.id.ns_map, View.VISIBLE);
            } else {
                v.setViewVisibility(R.id.ns_map, View.GONE);
            }
        }
        if (openPi != null) v.setOnClickPendingIntent(R.id.ns_root, openPi);
        mgr.updateAppWidget(ids, v);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        draw(context, mgr, ids);
    }
}
